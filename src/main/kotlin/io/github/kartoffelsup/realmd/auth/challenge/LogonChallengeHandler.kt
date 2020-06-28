package io.github.kartoffelsup.realmd.auth.challenge

import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.dml.SQLUpdateClause
import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.CommandHandler
import io.github.kartoffelsup.realmd.auth.AccountDb
import io.github.kartoffelsup.realmd.auth.AuthResult
import io.github.kartoffelsup.realmd.auth.SecureRemotePasswordProtocol
import io.github.kartoffelsup.realmd.auth.Srp6Values
import io.github.kartoffelsup.realmd.bean.AccountBean
import io.github.kartoffelsup.realmd.networking.IClientHandler
import io.github.kartoffelsup.realmd.sql.QAccount
import io.github.kartoffelsup.realmd.util.toHexadecimalString
import io.github.kartoffelsup.realmd.util.toReversedByteArray
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.concurrent.ThreadLocalRandom

class LogonChallengeHandler(
  private val accountDb: AccountDb,
  private val sqlQueryFactory: SQLQueryFactory,
  private val srp6Function: (AccountBean) -> Srp6Values = SecureRemotePasswordProtocol::calculateSrp6
) : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? {
    val unknownPart = BigInteger(16 * 8, ThreadLocalRandom.current()).toReversedByteArray(16)

    val authLogonParams: LogonChallengeParams? = LogonChallengeParser.parse(input)
    return authLogonParams?.let { params ->
      val accountDto = accountDb.run { sqlQueryFactory.findAccount(params.username) }
      val srp6Values = accountDto?.let(srp6Function)
      srp6Values?.let { srp6 ->
        clientHandler.srp6Values = srp6Values
        clientHandler.login = params.username

        val sToStore = srp6.s.toHexadecimalString()
        val vToStore = srp6.v.toHexadecimalString()

        accountDb.run {
          sqlQueryFactory.update(params.username) {
            set(QAccount.account.s, sToStore)
            set(QAccount.account.v, vToStore)
          }
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        return byteArrayOutputStream.apply {
          write(Command.AUTH_LOGON_CHALLENGE.value.toInt())
          write(0)
          write(AuthResult.WOW_SUCCESS.value.toInt())
          write(srp6.upperB.toReversedByteArray(32), 0, 32)
          write(1)
          write(srp6.g.toByteArray(), 0, 1)
          write(32)
          write(srp6.N.toReversedByteArray(32), 0, 32)
          write(srp6.s.toReversedByteArray(32), 0, 32)
          write(unknownPart, 0, 16)
          write(0)
        }.toByteArray()
      }
    }
  }
}
