package io.github.kartoffelsup.realmd.util

import java.math.BigInteger

fun positiveBigInteger(byteArray: ByteArray): BigInteger {
  return BigInteger(1, byteArray)
}

fun BigInteger.toReversedByteArray(minLength: Int = 0): ByteArray {
  val byteArray = this.toByteArray().stripLeadingZeros()
  var result: ByteArray = byteArray
  if (byteArray.size < minLength) {
    val filler = ByteArray(minLength - byteArray.size)
    filler.fill(0)
    result = filler + byteArray
  }

  return result.reversedArray()
}

fun BigInteger.toHexadecimalString(): String {
  return this.toByteArray().stripLeadingZeros().toHexadecimalString()
}
