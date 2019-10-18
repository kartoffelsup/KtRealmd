package io.github.kartoffelsup.realmd.auth.proof

import io.github.kartoffelsup.realmd.Command
import java.math.BigInteger

data class LogonProofParams(
  val cmd: Command,
  val a: BigInteger,
  val m1: BigInteger,
  val crcHash: BigInteger,
  val numberOfKeys: Byte,
  val securityFlags: Byte
)
