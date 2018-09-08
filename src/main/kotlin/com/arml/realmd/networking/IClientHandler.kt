package com.arml.realmd.networking

import com.arml.realmd.auth.Srp6Values

interface IClientHandler {
  var srp6Values: Srp6Values?
  var login: String?
  val ip: String
}