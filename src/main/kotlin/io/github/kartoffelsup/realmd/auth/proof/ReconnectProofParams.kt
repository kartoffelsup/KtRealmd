package io.github.kartoffelsup.realmd.auth.proof

import io.github.kartoffelsup.realmd.Command
import java.math.BigInteger

data class ReconnectProofParams(
  val cmd: Command,
  val r1: BigInteger,
  val r2: BigInteger,
  val r3: BigInteger,
  val numberOfKeys: Byte
)
