package com.arml.realmd.auth.challenge

import com.arml.realmd.Command
import com.arml.realmd.CommandHandler
import com.arml.realmd.auth.AccountDbOps
import com.arml.realmd.auth.AuthResult
import com.arml.realmd.auth.calculateSrp6
import com.arml.realmd.networking.ClientHandler
import com.arml.realmd.util.toHexadecimalString
import com.arml.realmd.util.toReversedByteArray
import java.math.BigInteger
import java.util.concurrent.ThreadLocalRandom

object LogonChallengeHandler : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: ClientHandler): ByteArray? {
    val unknownPart = BigInteger(16 * 8, ThreadLocalRandom.current()).toReversedByteArray(16)

    val baseResponse = byteArrayOf(
      Command.AUTH_LOGON_CHALLENGE.value, 0x00,
      AuthResult.WOW_SUCCESS.value
    )

    val authLogonParams: LogonChallengeParams? = LogonChallengeParser.parse(input)
    return authLogonParams?.let { params ->
      val accountDto = AccountDbOps.findAccount(params.username)
      val srp6Values = accountDto?.let(::calculateSrp6)
      srp6Values?.let { srp6 ->
        clientHandler.srp6Values = srp6Values
        clientHandler.login = params.username

        val vToStore = srp6.v.toHexadecimalString()
        val sToStore = srp6.s.toHexadecimalString()

        AccountDbOps.update(params.username) { acc ->
          acc[s] = sToStore
          acc[v] = vToStore
        }

        baseResponse
          .plus(srp6.B.toReversedByteArray(32))
          .plus(1)
          .plus(srp6.g.toReversedByteArray(1))
          .plus(32)
          .plus(srp6.N.toReversedByteArray(32))
          .plus(srp6.s.toReversedByteArray(32))
          .plus(unknownPart)
          .plus(0)
      }
    }
  }
}
