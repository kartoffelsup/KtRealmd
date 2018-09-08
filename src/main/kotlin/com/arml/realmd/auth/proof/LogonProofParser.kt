package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import com.arml.realmd.findCmd
import com.arml.realmd.util.positiveBigInteger
import com.arml.realmd.util.toHexadecimalString

object LogonProofParser {
  fun parse(input: ByteArray): LogonProofParams? {
    val cmd = input[0]
    val command = findCmd(cmd)?.takeIf { it == Command.AUTH_LOGON_PROOF }

    return command?.let {
      val a = positiveBigInteger(input.sliceArray(1..32).reversedArray())
      val m1 = positiveBigInteger(input.sliceArray(33..52).reversedArray())
      val crcHash = positiveBigInteger(input.sliceArray(53..72).reversedArray())
      val numberOfKeys = input.sliceArray(73..73)[0]
      val securityFlags = input.sliceArray(74..74)[0]

      LogonProofParams(
        Command.AUTH_LOGON_PROOF,
        a,
        m1,
        crcHash,
        numberOfKeys,
        securityFlags
      )
    }
  }
}
