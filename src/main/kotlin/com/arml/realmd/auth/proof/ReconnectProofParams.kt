package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import java.math.BigInteger

data class ReconnectProofParams(
  val cmd: Command,
  val r1: BigInteger,
  val r2: BigInteger,
  val r3: BigInteger,
  val numberOfKeys: Byte
)
