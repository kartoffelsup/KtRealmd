package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import com.arml.realmd.findCmd
import com.arml.realmd.util.positiveBigInteger

object ReconnectProofParser {
  fun parse(input: ByteArray): ReconnectProofParams? {
    val cmd = input[0]
    val command = findCmd(cmd)?.takeIf { it == Command.AUTH_RECONNECT_PROOF }

    return command?.let {
      val r1 = positiveBigInteger(input.sliceArray(1..16).reversedArray())
      val r2 = positiveBigInteger(input.sliceArray(17..36))
      val r3 = positiveBigInteger(input.sliceArray(37..56).reversedArray())
      val numberOfKeys = input.sliceArray(57..57)[0]

      ReconnectProofParams(
        Command.AUTH_LOGON_PROOF,
        r1,
        r2,
        r3,
        numberOfKeys
      )
    }
  }
}
