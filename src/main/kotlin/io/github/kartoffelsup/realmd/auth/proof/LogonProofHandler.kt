package io.github.kartoffelsup.realmd.auth.proof

import com.google.common.annotations.VisibleForTesting
import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.CommandHandler
import io.github.kartoffelsup.realmd.auth.AccountDb
import io.github.kartoffelsup.realmd.auth.AuthResult
import io.github.kartoffelsup.realmd.auth.Srp6Values
import io.github.kartoffelsup.realmd.networking.IClientHandler
import io.github.kartoffelsup.realmd.sql.QAccount
import io.github.kartoffelsup.realmd.util.positiveBigInteger
import io.github.kartoffelsup.realmd.util.sha1
import io.github.kartoffelsup.realmd.util.toHexadecimalString
import io.github.kartoffelsup.realmd.util.toReversedByteArray
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import kotlin.experimental.xor

class LogonProofHandler(
  private val accountDb: AccountDb,
  private val sqlQueryFactory: SQLQueryFactory
) : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? {
    val proofParamsOpt = LogonProofParser.parse(input)
    return proofParamsOpt?.let { proofParams ->
      val srp6Values = clientHandler.srp6Values
      srp6Values?.let { srp6 ->
        clientHandler.login?.let { login ->
          val proofResult: ProofResult = calculateProofResult(proofParams.a, srp6, login)

          // m1 is the SRP6Result from the client
          if (proofResult.m != proofParams.m1) {
            return byteArrayOf(
              Command.AUTH_LOGON_PROOF.value,
              AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.value
            )
          }

          accountDb.run {
            sqlQueryFactory.update(login) {
              set(QAccount.account.sessionkey, proofResult.k.toHexadecimalString())
              set(QAccount.account.lastIp, clientHandler.ip)
              set(QAccount.account.lastLogin, Timestamp.from(Instant.now()))
            }
          }
          
          val m2 = sha1 {
            update(proofParams.a.toReversedByteArray())
            update(proofResult.m.toReversedByteArray())
            update(proofResult.k.toReversedByteArray())
          }

          val byteBuffer = ByteBuffer.allocate(26)
          return byteBuffer.apply {
            put(Command.AUTH_LOGON_PROOF.value)
            put(0)
            put(m2)
            put(0)
            put(0)
            put(0)
            put(0)
          }.array()
        }
      }
    }
  }

  @VisibleForTesting
  internal fun calculateProofResult(
    a: BigInteger,
    srp6: Srp6Values,
    login: String
  ): ProofResult {
    val aBSha1 = sha1 {
      update(a.toReversedByteArray())
      update(srp6.upperB.toReversedByteArray())
    }
    val u = positiveBigInteger(aBSha1.reversedArray())
    val s = (a * (srp6.v.modPow(u, srp6.N))).modPow(srp6.lowerB, srp6.N)

    val t = s.toReversedByteArray(32)
    val t1 = t.filterIndexed { index, _ -> index % 2 == 0 }.toByteArray()
    val t1Sha1 = t1.sha1()
    val vk = vk(t1Sha1)
    for (i in 0 until 16) {
      t1[i] = t[i * 2 + 1]
    }
    val t1Sha1Two = t1.sha1()
    vk2(vk, t1Sha1Two)
    val k = positiveBigInteger(vk.reversedArray())
    val nSha1 = sha1(srp6.N)
    val gSha1 = sha1(srp6.g)
    val nSha1XoredByGSha1 = ByteArray(20)
    for (i in 0 until 20) {
      nSha1XoredByGSha1[i] = nSha1[i] xor gSha1[i]
    }
    val t3 = positiveBigInteger(nSha1XoredByGSha1.reversedArray())
    val t4 = login.toByteArray().sha1()
    val m = positiveBigInteger(sha1 {
      update(t3.toReversedByteArray())
      update(t4)
      update(srp6.s.toReversedByteArray())
      update(a.toReversedByteArray())
      update(srp6.upperB.toReversedByteArray())
      update(k.toReversedByteArray())
    }.reversedArray())
    return ProofResult(k, m)
  }

  private fun vk(t1Sha1: ByteArray): ByteArray {
    val vk = ByteArray(40)
    for (i in 0..19) {
      vk[i * 2] = t1Sha1[i]
    }
    return vk
  }

  private fun vk2(vk: ByteArray, t1Sha1Two: ByteArray) {
    for (i in 0..19) {
      vk[i * 2 + 1] = t1Sha1Two[i]
    }
  }
}

inline fun sha1(ops: MessageDigest.() -> Unit): ByteArray {
  val messageDigest = MessageDigest.getInstance("SHA1")
  ops(messageDigest)
  return messageDigest.digest()
}

fun sha1(bigInteger: BigInteger): ByteArray {
  val messageDigest = MessageDigest.getInstance("SHA1")
  messageDigest.update(bigInteger.toReversedByteArray())
  return messageDigest.digest()
}

class ProofResult(val k: BigInteger, val m: BigInteger)
