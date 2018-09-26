package com.arml.realmd.networking

import com.arml.realmd.auth.Srp6Values
import java.math.BigInteger

interface IClientHandler {
  var srp6Values: Srp6Values?
  var login: String?
  val ip: String
  var sessionKey: String?
  var reconnectProof: BigInteger?
}
