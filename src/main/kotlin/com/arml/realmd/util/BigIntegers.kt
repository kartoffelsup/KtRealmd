package com.arml.realmd.util

import java.math.BigInteger

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
