package com.arml.realmd.auth.challenge

import com.arml.realmd.Command

data class LogonChallengeParams(
  val cmd: Command,
  val error: Byte,
  val remainingSize: Int,
  val gameName: String,
  val version1: Byte,
  val version2: Byte,
  val version3: Byte,
  val build: Int,
  val platform: String,
  val os: String,
  val country: String,
  val timezoneBias: Int,
  val ip: String,
  val usernameLength: Byte,
  val username: String
)
