package io.github.kartoffelsup.realmd.auth.challenge

import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.findCmd
import io.github.kartoffelsup.realmd.util.littleEndianInt
import io.github.kartoffelsup.realmd.util.reverseToString
import io.github.kartoffelsup.realmd.util.toUtf8

object LogonChallengeParser {
  fun parse(input: ByteArray): LogonChallengeParams? {
    return parse(input, Command.AUTH_LOGON_CHALLENGE)
  }

  fun parseForReconnect(input: ByteArray): LogonChallengeParams? {
    return parse(input, Command.AUTH_RECONNECT_CHALLENGE)
  }

  private fun parse(input: ByteArray, forCmd: Command): LogonChallengeParams? {
    val cmd = input[0]
    val command: Command? = findCmd(cmd)?.takeIf { it == forCmd }

    return command?.let {
      val err = input[1]
      val remainingSize = input.sliceArray(2..3).littleEndianInt()
      val gameName = input.sliceArray(4..7).reverseToString()
      val (version1, version2, version3) = input.sliceArray(8..10)
      val build = input.sliceArray(11..12).littleEndianInt()
      val platform = input.sliceArray(13..16).reverseToString()
      val os = input.sliceArray(17..20).reverseToString()
      val country = input.sliceArray(21..24).reverseToString()
      val timezoneBias = input.sliceArray(25..28).littleEndianInt()
      val ip = input.sliceArray(29..33).contentToString()
      val usernameLength = input.sliceArray(33..34)[0]
      val username = input.sliceArray(34..(34 + (usernameLength - 1))).toUtf8()

      LogonChallengeParams(
        Command.AUTH_LOGON_CHALLENGE,
        err,
        remainingSize,
        gameName,
        version1,
        version2,
        version3,
        build,
        platform,
        os,
        country,
        timezoneBias,
        ip,
        usernameLength,
        username
      )
    }
  }
}
