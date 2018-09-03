package com.arml.realmd.auth.proof

import com.arml.realmd.CommandHandler
import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.networking.ClientHandler
import com.arml.realmd.util.sha1
import com.arml.realmd.util.toReversedByteArray
import com.google.common.annotations.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.experimental.xor

object LogonProofHandler : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: ClientHandler): ByteArray? {
    val proofParamsOpt = LogonProofParser.parse(input)
    return proofParamsOpt?.let { proofParams ->
      val a = BigInteger(proofParams.a, 16)
      val srp6Values = clientHandler.srp6Values
      srp6Values?.let { srp6 ->
        clientHandler.login?.let { calculateM(a, srp6, it) }
        TODO("handle")
      }
    }
  }

  @VisibleForTesting
  internal fun calculateM(
    a: BigInteger,
    srp6: Srp6Values,
    login: String
  ): BigInteger {
    val aBSha1 = sha1 {
      update(a.toReversedByteArray())
      update(srp6.B.toReversedByteArray())
    }
    val u = BigInteger(1, aBSha1.reversedArray())
    val s = (a * (srp6.v.modPow(u, srp6.N))).modPow(srp6.lowerB, srp6.N)

    val t = s.toReversedByteArray(32)
    val t1 = t.filterIndexed { index, _ -> index % 2 == 0 }.toByteArray()
    val t1Sha1 = t1.sha1()
    val vk = vk(t1Sha1)
    for (i in 0..15) {
      t1[i] = t[i * 2 + 1]
    }
    val t1Sha1Two = t1.sha1()
    vk2(vk, t1Sha1Two)
    val k = BigInteger(1, vk.reversedArray())
    val nSha1 = sha1(srp6.N)
    val gSha1 = sha1(srp6.g)
    val nSha1XoredByGSha1 = ByteArray(20)
    for (i in 0..19) {
      nSha1XoredByGSha1[i] = nSha1[i] xor gSha1[i]
    }
    val t3 = BigInteger(1, nSha1XoredByGSha1.reversedArray())
    val t4 = login.toByteArray().sha1()
    return BigInteger(1, sha1 {
      update(t3.toReversedByteArray())
      update(t4)
      update(srp6.s.toReversedByteArray())
      update(a.toReversedByteArray())
      update(srp6.B.toReversedByteArray())
      update(k.toReversedByteArray())
    }.reversedArray())
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
