package com.arml.realmd.auth.challenge

import com.arml.realmd.Command
import com.arml.realmd.CommandHandler
import com.arml.realmd.auth.AccountDb
import com.arml.realmd.auth.AccountDto
import com.arml.realmd.auth.AuthResult
import com.arml.realmd.auth.SecureRemotePasswordProtocol
import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.networking.IClientHandler
import com.arml.realmd.util.toHexadecimalString
import com.arml.realmd.util.toReversedByteArray
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.util.concurrent.ThreadLocalRandom

class LogonChallengeHandler(
  private val accountDb: AccountDb,
  private val srp6Function: (AccountDto) -> Srp6Values = SecureRemotePasswordProtocol::calculateSrp6
) : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? {
    println("LogonChallenge: handle")
    val unknownPart = BigInteger(16 * 8, ThreadLocalRandom.current()).toReversedByteArray(16)

    val authLogonParams: LogonChallengeParams? = LogonChallengeParser.parse(input)
    return authLogonParams?.let { params ->
      val accountDto = accountDb.findAccount(params.username)
      val srp6Values = accountDto?.let(srp6Function)
      srp6Values?.let { srp6 ->
        clientHandler.srp6Values = srp6Values
        clientHandler.login = params.username

        val vToStore = srp6.v.toHexadecimalString()
        val sToStore = srp6.s.toHexadecimalString()

        accountDb.update(params.username) { acc ->
          acc[s] = sToStore
          acc[v] = vToStore
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        println("LogonChallenge: reply")
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
