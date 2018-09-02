package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import com.arml.realmd.findCmd
import com.arml.realmd.util.toHexadecimalString

object LogonProofParser {
  fun parse(input: ByteArray): LogonProofParams? {
    val cmd = input[0]
    val command = findCmd(cmd)?.takeIf { it == Command.AUTH_LOGON_PROOF }

    return command?.let {
      val a = input.sliceArray(1..32).toHexadecimalString()
      val m1 = input.sliceArray(32..51).toHexadecimalString()
      val crcHash = input.sliceArray(52..71).toHexadecimalString()
      val numberOfKeys = input.sliceArray(72..73)[0]
      val securityFlags = input.sliceArray(74..75)[0]

      LogonProofParams(
        Command.AUTH_LOGON_CHALLENGE,
        a,
        m1,
        crcHash,
        numberOfKeys,
        securityFlags
      )
    }
  }
}
