package io.github.kartoffelsup.realmd.util

import com.google.common.io.BaseEncoding

fun String.hexStringToByteArray(): ByteArray =
  BaseEncoding
    .base16()
    .decode(this.toUpperCase())
