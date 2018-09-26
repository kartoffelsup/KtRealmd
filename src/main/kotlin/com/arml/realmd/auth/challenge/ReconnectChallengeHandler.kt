package com.arml.realmd.auth.challenge

import com.arml.realmd.Command
import com.arml.realmd.CommandHandler
import com.arml.realmd.auth.AccountDb
import com.arml.realmd.networking.IClientHandler
import com.arml.realmd.util.toReversedByteArray
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.concurrent.ThreadLocalRandom

class ReconnectChallengeHandler(
  private val accountDb: AccountDb
) : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? {
    return LogonChallengeParser.parse(input)?.let { authLogonParams ->
      val sessionKey = accountDb.findSessionKey(authLogonParams.username)
      val reconnectProof = BigInteger(16 * 8, ThreadLocalRandom.current())

      clientHandler.apply {
        this.login = authLogonParams.username
        this.sessionKey = sessionKey
        this.reconnectProof = reconnectProof
      }

      return ByteBuffer.allocate(32).apply {
        put(Command.AUTH_RECONNECT_CHALLENGE.value)
        put(0)
        put(reconnectProof.toReversedByteArray())
        put(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
      }.array()
    }
  }
}
