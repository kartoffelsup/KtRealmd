package com.arml.realmd.auth

import com.arml.realmd.util.hexStringToByteArray
import com.arml.realmd.util.toHexadecimalString
import com.arml.realmd.util.toReversedByteArray
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.ThreadLocalRandom

const val sByteSize = 32
const val sHexSize = sByteSize * 2
const val sBitSize = sByteSize * 8

val g: BigInteger = BigInteger.valueOf(7L)
const val N: String = "894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7"
val nBigInteger: BigInteger = BigInteger(N, 16)

data class Srp6Values(
  val B: BigInteger,
  val lowerB: BigInteger,
  val g: BigInteger,
  val N: BigInteger,
  val v: BigInteger,
  val s: BigInteger,
  val x: BigInteger
)

fun calculateSrp6(accountDto: AccountDto): Srp6Values {
  return calculateSrp6Internal(accountDto)
}

internal fun calculateSrp6ForTest(accountDto: AccountDto, b: BigInteger): Srp6Values {
  return calculateSrp6Internal(accountDto, b)
}

private fun calculateSrp6Internal(accountDto: AccountDto, b: BigInteger? = null): Srp6Values {
  val sFromDb = accountDto.s
  val vFromDb = accountDto.v
  val passHash = accountDto.shaPassHash
  val passBytes = passHash.hexStringToByteArray()
  val s = sFromDb?.let {
    BigInteger(it, 16)
  } ?: BigInteger(sBitSize, ThreadLocalRandom.current())

  val messageDigest = MessageDigest.getInstance("SHA1")
  messageDigest.apply {
    update(s.toReversedByteArray())
    update(passBytes)
  }

  val digest = messageDigest.digest()
  val x = BigInteger(digest.reversedArray().toHexadecimalString(), 16)
  val vBigInt = vFromDb?.let { BigInteger(it, 16) } ?: g.modPow(x, nBigInteger)
  val bToUse = b ?: BigInteger(19 * 8, ThreadLocalRandom.current())
  val gmod = g.modPow(bToUse, nBigInteger)
  check(gmod.toByteArray().size >= 32) { "gmod must have a bytarray size >= 32" }
  val upperB = ((vBigInt * BigInteger.valueOf(3L)) + gmod) % nBigInteger

  return Srp6Values(upperB, bToUse, g, nBigInteger, vBigInt, s, x)
}
