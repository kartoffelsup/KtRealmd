package com.arml.realmd.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigInteger

class BigIntegersTest {

  @Test
  fun toHexadecimalString() {
    val hexaDecimal = "07381756684AEBA4D0C0E4001F144CA154B91F53F53B085329E6ED9F573C7B6F"
    assertThat(BigInteger(hexaDecimal, 16).toHexadecimalString())
      .isEqualTo("07381756684AEBA4D0C0E4001F144CA154B91F53F53B085329E6ED9F573C7B6F")
  }
}