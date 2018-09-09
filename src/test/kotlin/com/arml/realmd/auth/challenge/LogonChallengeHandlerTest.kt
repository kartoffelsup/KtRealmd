package com.arml.realmd.auth.challenge

import com.arml.realmd.auth.Account
import com.arml.realmd.auth.AccountDb
import com.arml.realmd.auth.AccountDto
import com.arml.realmd.auth.SecureRemotePasswordProtocol
import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.auth.accountDto
import com.arml.realmd.networking.IClientHandler
import com.arml.realmd.util.toHexadecimalString
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.junit.Test
import java.math.BigInteger

val b = BigInteger("4256674378144676270607276026758948068047056529")
val input =
  BigInteger("49486913718021746441250803648978276699302210686036230446916379472485252247460027053942952521760566535127235756800").toByteArray()
val expected: String = "EE5772222579A1C3C61176E02B6EDF27111F2943538EAD8FB858842EAEF8FFDB894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB720070181B6491120CFBC1E6693ADEEA5AE8F858EAA78ECD6DC8C30AB9A1B4B0078E824000000"

class LogonChallengeHandlerTest {
  private val logonChallengeHandler: LogonChallengeHandler =
    LogonChallengeHandler(AccountDbMock) {
      SecureRemotePasswordProtocol.calculateSrp6ForTest(it, b)
    }

  @Test
  fun testHandle() {
    val response: ByteArray? = logonChallengeHandler.handle(input, ClientHandlerMock)
    val actualWithoutRandomUnknownPart = response?.let { it.sliceArray(17 until it.size) }

    actualWithoutRandomUnknownPart?.let {
      assertThat(it.toHexadecimalString())
        .isEqualTo(expected)
    }
  }
}

object AccountDbMock : AccountDb {
  override fun update(username: String, body: Account.(UpdateStatement) -> Unit): Int = 0
  override fun findAccount(username: String): AccountDto? = accountDto
}

object ClientHandlerMock : IClientHandler {
  override var srp6Values: Srp6Values? = null
  override var login: String? = null
  override val ip: String = "127.0.0.1"
}
