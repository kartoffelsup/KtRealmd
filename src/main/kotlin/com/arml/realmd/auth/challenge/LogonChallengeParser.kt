package com.arml.realmd.auth.challenge

import com.arml.realmd.Command
import com.arml.realmd.findCmd
import com.arml.realmd.util.littleEndianInt
import com.arml.realmd.util.reverseToString
import com.arml.realmd.util.toUtf8

object LogonChallengeParser {
  fun parse(input: ByteArray): LogonChallengeParams? {
    val cmd = input[0]
    val command: Command? = findCmd(cmd)?.takeIf { it == Command.AUTH_LOGON_CHALLENGE }

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
