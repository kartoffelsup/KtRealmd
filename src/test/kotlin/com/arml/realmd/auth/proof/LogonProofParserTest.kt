package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import com.arml.realmd.util.positiveBigInteger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LogonProofParserTest {

  @ExperimentalUnsignedTypes
  @Test
  fun parse() {
    val input = ubyteArrayOf(
      1,

      16, 40, 70, 58, 134,
      226, 168, 125, 63, 192,
      172, 183, 248, 121, 235,
      141, 86, 227, 64, 153,
      205, 53, 116, 16, 238,
      19, 218, 55, 205, 245,
      37, 26,

      203, 175, 106, 121, 251,
      209, 108, 3, 164, 247,
      27, 203, 240, 42, 111,
      116, 140, 248, 240, 96,

      92, 19, 244, 124, 106,
      62, 239, 214, 39, 96,
      218, 102, 158, 81, 184,
      163, 68, 123, 133, 239,

      0,
      0
    ).toByteArray()

    val expected = LogonProofParams(
      cmd = Command.AUTH_LOGON_PROOF,
      a = positiveBigInteger(
        ubyteArrayOf(
          16, 40, 70, 58, 134,
          226, 168, 125, 63, 192,
          172, 183, 248, 121, 235,
          141, 86, 227, 64, 153,
          205, 53, 116, 16, 238,
          19, 218, 55, 205, 245,
          37, 26
        ).toByteArray().reversedArray()
      ),
      m1 = positiveBigInteger(
        ubyteArrayOf(
          203, 175, 106, 121, 251,
          209, 108, 3, 164, 247,
          27, 203, 240, 42, 111,
          116, 140, 248, 240, 96
        ).toByteArray().reversedArray()
      ),
      crcHash = positiveBigInteger(
        ubyteArrayOf(
          92, 19, 244, 124, 106,
          62, 239, 214, 39, 96,
          218, 102, 158, 81, 184,
          163, 68, 123, 133, 239
        ).toByteArray().reversedArray()
      ),
      numberOfKeys = 0,
      securityFlags = 0
    )

    val actual = LogonProofParser.parse(input)
    assertThat(actual).isNotNull

    if (actual != null) {
      assertThat(actual).isEqualTo(expected)
    }
  }
}
