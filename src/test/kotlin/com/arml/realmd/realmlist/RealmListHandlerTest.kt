package com.arml.realmd.realmlist

import com.arml.realmd.Command
import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.networking.IClientHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val expectedResponse = ubyteArrayOf(
  16, 76, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 2, 77, 97, 78, 71, 79, 83, 0, 49,
  50, 55, 46, 48, 46, 48, 46, 49, 58, 56, 48, 56, 53, 0, 0, 0, 0, 0,
  3, 1, 0, 1, 0, 0, 0, 2, 77, 97, 78, 71, 79, 83, 50, 0,
  49, 50, 55, 46, 48, 46, 48, 46, 49, 58, 56, 48,
  56, 54, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0
).toByteArray()

class RealmListHandlerTest {
  private val realmListHandler = RealmListHandler(RealmListDbMock)

  @Test
  fun testHandle() {
    val actual = realmListHandler.handle(byteArrayOf(Command.REALM_LIST.value), ClientHandlerMock)
    assertThat(actual).isEqualTo(expectedResponse)
  }
}

object RealmListDbMock : RealmListDb {
  override fun findNumChars(login: String): List<Pair<RealmListDto, Int?>> {
    return listOf(
      RealmListDto(1, "MaNGOS", "127.0.0.1", 8085, 1, 2, 1, 0, 0.0f, "") to 3,
      RealmListDto(2, "MaNGOS2", "127.0.0.1", 8086, 1, 2, 1, 0, 0.0f, "") to 0
    )
  }
}

object ClientHandlerMock : IClientHandler {
  override var srp6Values: Srp6Values? = null
  override var login: String? = "ADMINISTRATOR"
  override val ip: String = "127.0.0.1"
}
