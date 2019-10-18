package io.github.kartoffelsup.realmd.auth.challenge

import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.CommandHandler
import io.github.kartoffelsup.realmd.auth.AccountDb
import io.github.kartoffelsup.realmd.networking.IClientHandler
import io.github.kartoffelsup.realmd.util.toReversedByteArray
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.concurrent.ThreadLocalRandom

class ReconnectChallengeHandler(
  private val accountDb: AccountDb,
  private val sqlQueryFactory: SQLQueryFactory
) : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? {
    return LogonChallengeParser.parseForReconnect(input)?.let { authLogonParams ->
      val sessionKey = accountDb.run { sqlQueryFactory.findSessionKey(authLogonParams.username) }
      val reconnectProof = BigInteger(16 * 8, ThreadLocalRandom.current())

      clientHandler.apply {
        this.login = authLogonParams.username
        this.sessionKey = sessionKey
        this.reconnectProof = reconnectProof
      }

      return ByteBuffer.allocate(34).apply {
        put(Command.AUTH_RECONNECT_CHALLENGE.value)
        put(0)
        put(reconnectProof.toReversedByteArray())
        put(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
      }.array()
    }
  }
}
