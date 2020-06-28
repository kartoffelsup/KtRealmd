package io.github.kartoffelsup.realmd.realmlist

import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.auth.Srp6Values
import io.github.kartoffelsup.realmd.bean.RealmlistBean
import io.github.kartoffelsup.realmd.networking.IClientHandler
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger

val expectedResponse = byteArrayOf(
    16, 76, 0, 0, 0, 0, 0, 2, 1, 0, 0, 0, 2, 77, 97, 78, 71, 79, 83, 0, 49,
    50, 55, 46, 48, 46, 48, 46, 49, 58, 56, 48, 56, 53, 0, 0, 0, 0, 0,
    3, 1, 0, 1, 0, 0, 0, 2, 77, 97, 78, 71, 79, 83, 50, 0,
    49, 50, 55, 46, 48, 46, 48, 46, 49, 58, 56, 48,
    56, 54, 0, 0, 0, 0, 0, 0, 1, 0, 2, 0
)

class RealmListHandlerTest {
    private val realmListHandler = RealmListHandler(RealmListDbMock, mockk())

    @Test
    fun testHandle() {
        val actual = realmListHandler.handle(byteArrayOf(Command.REALM_LIST.value), ClientHandlerMock)
        assertThat(actual).isEqualTo(expectedResponse)
    }
}

object RealmListDbMock : RealmListDb {
    override fun SQLQueryFactory.findNumChars(login: String): List<Pair<RealmlistBean, Int?>> {
        return listOf(
            RealmlistBean(
                id = 1,
                name = "MaNGOS",
                address = "127.0.0.1",
                port = 8085,
                icon = 1,
                realmflags = 2,
                timezone = 1,
                allowedSecurityLevel = 0,
                population = 0.0f,
                realmbuilds = ""
            ) to 3,
            RealmlistBean(
              id = 2,
              name = "MaNGOS2",
              address = "127.0.0.1",
              port = 8086,
              icon = 1,
              realmflags = 2,
              timezone = 1,
              allowedSecurityLevel = 0,
              population = 0.0f,
              realmbuilds = "") to 0
        )
    }
}

object ClientHandlerMock : IClientHandler {
    override var sessionKey: String? = null
    override var reconnectProof: BigInteger? = null
    override var srp6Values: Srp6Values? = null
    override var login: String? = "ADMINISTRATOR"
    override val ip: String = "127.0.0.1"
}
