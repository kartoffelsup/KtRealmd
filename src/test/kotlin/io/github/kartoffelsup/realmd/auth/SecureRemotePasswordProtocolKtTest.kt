package io.github.kartoffelsup.realmd.auth

import io.github.kartoffelsup.realmd.bean.AccountBean
import io.github.kartoffelsup.realmd.util.toHexadecimalString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigInteger
import java.sql.Timestamp
import java.time.Instant

val accountDto = AccountBean(
    id = 1,
    username = "ADMINISTRATOR",
    shaPassHash = "A34B29541B87B7E4823683CE6C7BF6AE68BEAAAC",
    gmlevel = 0,
    sessionkey = "",
    v = "7FEE361FF13EA1DCF9C44F2252DAB77E86F056AB77306176859F2D352ECE52D4",
    s = "EE5772222579A1C3C61176E02B6EDF27111F2943538EAD8FB858842EAEF8FFDB",
    email = "",
    joindate = Timestamp.from(Instant.now()),
    lastIp = "",
    failedLogins = 0,
    locked = 0,
    lastLogin = Timestamp.from(Instant.now()),
    activeRealmId = 0,
    expansion = 0,
    mutetime = 0L,
    locale = 0,
    token = null
)

val accountDtoForVTest =
    accountDto.copy(s = "83917129C4D1BD25807A373FC05051676D369CD9B8D263E971B7385B36F48B99", v = null)

class SecureRemotePasswordProtocolTest {
    @Test
    fun testCalculateSrp6() {
        val srp6Values: Srp6Values =
            SecureRemotePasswordProtocol.calculateSrp6ForTest(
                accountDto,
                BigInteger("4591715913358148007112195511131027575013655409")
            )

        assertThat(srp6Values.s)
            .withFailMessage("Expecting 's' to equal '107804961377121431927430582703752344117406575734048882862298890256631438311387' but was '${srp6Values.s}'")
            .isEqualTo(BigInteger("107804961377121431927430582703752344117406575734048882862298890256631438311387"))
        assertThat(srp6Values.v)
            .withFailMessage("Expecting 'v' to equal '57864614926959187882839881343938881234107798018904538741830451870910647259860' but was '${srp6Values.v}'")
            .isEqualTo(BigInteger("57864614926959187882839881343938881234107798018904538741830451870910647259860"))
        assertThat(srp6Values.upperB)
            .withFailMessage("Expecting 'upperB' to equal '38927182161773898413198331338929741446046539040589318629507861288915789776482' but was '${srp6Values.upperB}'")
            .isEqualTo(BigInteger("38927182161773898413198331338929741446046539040589318629507861288915789776482"))
        assertThat(srp6Values.x)
            .withFailMessage("Expecting 'x' to equal '571435518187733976510119898806407003928090887562' but was '${srp6Values.x}'")
            .isEqualTo(BigInteger("571435518187733976510119898806407003928090887562"))
    }

    @Test
    fun testCalculateVFromS() {
        val srp6Values: Srp6Values =
            SecureRemotePasswordProtocol.calculateSrp6ForTest(
                accountDtoForVTest,
                BigInteger("4591715913358148007112195511131027575013655409")
            )

        val sHex = srp6Values.s.toHexadecimalString()
        assertThat(sHex)
            .isEqualTo("83917129C4D1BD25807A373FC05051676D369CD9B8D263E971B7385B36F48B99")

        val xHex = srp6Values.x.toHexadecimalString()
        assertThat(xHex)
            .isEqualTo("84BB20BD460B64BBF667BB42E8BA5B5984D3EEBF")

        val vHex = srp6Values.v.toHexadecimalString()
        assertThat(vHex)
            .isEqualTo("81C951B8972C3BE3860B51BA0E6A63EE496F3B8ABFB6F4CA49E4BC71D4B8B209")
    }

    @Test
    fun testCalculateSrp6_SAndVSizes() {
        val srp6Values: Srp6Values = SecureRemotePasswordProtocol.calculateSrp6(accountDto)

        assertThat(srp6Values.s.toHexadecimalString())
            .hasSize(64)

        assertThat(srp6Values.v.toHexadecimalString())
            .hasSize(64)
    }
}
