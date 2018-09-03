package com.arml.realmd.auth.proof

import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.util.toHexadecimalString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.math.BigInteger

class LogonProofHandlerTest {

  @Before
  fun setUp() {
  }

  @Test
  fun testCalculateM() {
    val a = BigInteger("3D90DDA9AB551BB014977CA07820A43DACA82C861546D55DEEF0D23699F6C1C5", 16)
    val srp6Values = Srp6Values(
      B = BigInteger("07381756684AEBA4D0C0E4001F144CA154B91F53F53B085329E6ED9F573C7B6F", 16),
      lowerB = BigInteger("A17981033ED6094D9B6DF9AE7D75C956AC6221", 16),
      g = BigInteger("7"),
      v = BigInteger("56A9FAE94E75707FE301D86CF5EE5822B8EE642497E04A07BAA295C3A69A9652", 16),
      s = BigInteger("D7268532550D4750FC2C54667DADAD51D0CF8DC89C6468F54C4FE2B34C5A3FB3", 16),
      x = BigInteger.ZERO
    )
    val login = "ADMINISTRATOR"
    val actual = LogonProofHandler.calculateM(a, srp6Values, login)
    val expected = "4897559225CAE24FF536A8FEE59C95EE0C5BB6AC"
    assertThat(actual.toHexadecimalString()).isEqualTo(expected)
  }
}
