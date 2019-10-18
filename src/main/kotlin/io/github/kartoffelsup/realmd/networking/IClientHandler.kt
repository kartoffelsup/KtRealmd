package io.github.kartoffelsup.realmd.networking

import io.github.kartoffelsup.realmd.auth.Srp6Values
import java.math.BigInteger

interface IClientHandler {
  var srp6Values: Srp6Values?
  var login: String?
  val ip: String
  var sessionKey: String?
  var reconnectProof: BigInteger?
}
