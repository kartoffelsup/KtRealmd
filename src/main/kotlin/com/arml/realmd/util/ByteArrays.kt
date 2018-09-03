package com.arml.realmd.util

import com.google.common.io.BaseEncoding
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

fun ByteArray.littleEndianInt(): Int {
  val delta = 4 - this.size
  val input: ByteArray
  input = if (delta > 0) {
    val filler = ByteArray(4 - this.size)
    filler.fill(0)
    this + filler
  } else {
    this
  }
  return ByteBuffer
    .wrap(input)
    .order(ByteOrder.LITTLE_ENDIAN)
    .int
}

const val nulByte: Byte = 0
fun ByteArray.reverseToString(): String {
  val reversedArray = reversedArray()
  val firstByte = reversedArray[0]
  if (firstByte == nulByte) {
    return reversedArray.sliceArray(1..(reversedArray.size - 1)).toUtf8()
  }
  return reversedArray.toUtf8()
}

fun ByteArray.toHexadecimalString(): String =
  BaseEncoding
    .base16()
    .encode(this)
    .toString()

fun ByteArray.toUtf8() = this.toString(Charsets.UTF_8)

fun ByteArray.stripLeadingZeros(): ByteArray {
  var lastZeroIndex = -1
  for (index in 0..(size - 1)) {
    val byte = this[index]
    if (byte == nulByte) {
      lastZeroIndex = index
    } else {
      break
    }
  }
  return if (lastZeroIndex == -1) {
    this
  } else {
    this.sliceArray((lastZeroIndex + 1)..(this.size - 1))
  }
}

fun ByteArray.sha1(): ByteArray {
  val messageDigest = MessageDigest.getInstance("SHA1")
  messageDigest.update(this)
  return messageDigest.digest()
}
