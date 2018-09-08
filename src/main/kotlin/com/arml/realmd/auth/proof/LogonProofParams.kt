package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import java.math.BigInteger

data class LogonProofParams(
  val cmd: Command,
  val a: BigInteger,
  val m1: BigInteger,
  val crcHash: BigInteger,
  val numberOfKeys: Byte,
  val securityFlags: Byte
)
