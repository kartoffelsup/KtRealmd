package io.github.kartoffelsup.realmd.auth.challenge

import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.util.toReversedByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigInteger

class LogonChallengeParserTest {

  @Test
  fun parse() {
    val input = byteArrayOf(
      0, 3, 43, 0,
      87, 111, 87, 0,
      1, 12, 1, -13,
      22, 54, 56, 120,
      0, 110, 105, 87,
      0, 83, 85, 110,
      101, 60, 0, 0,
      0, 127, 0, 0,
      1, 13, 65, 68,
      77, 73, 78, 73,
      83, 84, 82, 65,
      84, 79, 82, 0
    )

    val expected = LogonChallengeParams(
      cmd = Command.AUTH_LOGON_CHALLENGE,
      error = 3,
      remainingSize = 43,
      gameName = "WoW",
      version1 = 1,
      version2 = 12,
      version3 = 1,
      build = 5875,
      platform = "x86",
      os = "Win",
      country = "enUS",
      timezoneBias = 60,
      ip = "[127, 0, 0, 1, 13]",
      usernameLength = 13,
      username = "ADMINISTRATOR"
    )

    val actual = LogonChallengeParser.parse(input)
    assertThat(actual).isNotNull

    if (actual != null) {
      assertThat(actual).isEqualTo(expected)
    }
  }

  @Test
  fun parse2() {
    val input =
      BigInteger("49486913718021746441250803648978276699302210686036230446916379472485252247460027053942952521760566535127235756800")
        .toReversedByteArray()

    val expected = LogonChallengeParams(
      cmd = Command.AUTH_LOGON_CHALLENGE,
      error = 3,
      remainingSize = 43,
      gameName = "WoW",
      version1 = 1,
      version2 = 12,
      version3 = 1,
      build = 5875,
      platform = "x86",
      os = "Win",
      country = "enUS",
      timezoneBias = 60,
      ip = "[127, 0, 0, 1, 13]",
      usernameLength = 13,
      username = "ADMINISTRATOR"
    )

    val actual = LogonChallengeParser.parse(input)
    assertThat(actual).isNotNull

    if (actual != null) {
      assertThat(actual).isEqualTo(expected)
    }
  }
}
