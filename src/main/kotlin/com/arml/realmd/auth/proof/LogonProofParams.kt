package com.arml.realmd.auth.proof

import com.arml.realmd.Command

data class LogonProofParams(
  val cmd: Command,
  val a: String,
  val m1: String,
  val crcHash: String,
  val numberOfKeys: Byte,
  val securityFlags: Byte
)
