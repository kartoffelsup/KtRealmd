package io.github.kartoffelsup.realmd.auth.proof

import io.github.kartoffelsup.realmd.Command
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger

class LogonProofParserTest {

  @Test
  fun parse() {
    val input = byteArrayOf(
      1, 16, 40, 70, 58, -122, -30, -88, 125, 63, -64,
      -84, -73, -8, 121, -21, -115, 86, -29, 64, -103,
      -51, 53, 116, 16, -18, 19, -38, 55, -51, -11, 37,
      26, -53, -81, 106, 121, -5, -47, 108, 3, -92, -9,
      27, -53, -16, 42, 111, 116, -116, -8, -16, 96, 92,
      19, -12, 124, 106, 62, -17, -42, 39, 96, -38, 102,
      -98, 81, -72, -93, 68, 123, -123, -17, 0, 0
    )

    val expected = LogonProofParams(
      cmd = Command.AUTH_LOGON_PROOF,
      a = BigInteger("11827203865087644838231244116194817610001689481834515960723261925173292836880"),
      m1 = BigInteger("553436944487946052754072863611667483787518455755"),
      crcHash = BigInteger("1367425531506293977663669427091677767798713750364"),
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
