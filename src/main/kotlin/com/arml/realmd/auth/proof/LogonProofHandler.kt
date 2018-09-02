package com.arml.realmd.auth.proof

import com.arml.realmd.CommandHandler
import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.networking.ClientHandler
import com.arml.realmd.util.sha1
import com.arml.realmd.util.toHexadecimalString
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
        val m = calculateM(a, srp6, clientHandler)
        println(proofParams.m1 == m?.toHexadecimalString())
        TODO("handle")
        // 3FDFBC38020BB08F78E7C0F39198E2DB2563262D
        // A963D764121AB4230CB5C0D66C84B805619E71AD
      }
    }
  }

  @VisibleForTesting
  internal fun calculateM(
    a: BigInteger,
    srp6: Srp6Values,
    clientHandler: ClientHandler
  ): BigInteger? {
    val aBSha1 = sha1(a, srp6.B)
    val u = BigInteger(aBSha1.toHexadecimalString(), 16)
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
    val k = BigInteger(vk.reversedArray().toHexadecimalString(), 16)
    val hash = sha1(srp6.N)
    val gHash = sha1(srp6.g)
    for (i in 0..19) {
      hash[i] = hash[i] xor gHash[i]
    }
    val t3 = BigInteger(hash.toHexadecimalString(), 16)
    val t4 = clientHandler.login?.toByteArray()?.sha1()?.toHexadecimalString()
      ?.let { BigInteger(it, 16) }
    return t4?.let { BigInteger(sha1(t3, t4, srp6.s, a, srp6.B, k).toHexadecimalString(), 16) }
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

fun sha1(vararg bigInts: BigInteger): ByteArray {
  val messageDigest = MessageDigest.getInstance("SHA1")
  messageDigest.apply {
    bigInts.map { it.toReversedByteArray() }.forEach(::update)
  }
  return messageDigest.digest()
}
