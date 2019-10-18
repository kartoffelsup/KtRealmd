package io.github.kartoffelsup.realmd.auth.proof

import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.dml.SQLUpdateClause
import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.auth.AccountDb
import io.github.kartoffelsup.realmd.auth.Srp6Values
import io.github.kartoffelsup.realmd.auth.accountDto
import io.github.kartoffelsup.realmd.bean.AccountBean
import io.github.kartoffelsup.realmd.networking.IClientHandler
import io.github.kartoffelsup.realmd.sql.QAccount
import io.github.kartoffelsup.realmd.util.positiveBigInteger
import io.github.kartoffelsup.realmd.util.toHexadecimalString
import io.github.kartoffelsup.realmd.util.toReversedByteArray
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.concurrent.ThreadLocalRandom

class LogonProofHandlerTest {

  private val logonProofHandler = LogonProofHandler(AccountDbMock, mockk())

  @Test
  fun testCalculateM() {
    val a = BigInteger("3D90DDA9AB551BB014977CA07820A43DACA82C861546D55DEEF0D23699F6C1C5", 16)
    val srp6Values = Srp6Values(
      upperB = BigInteger("07381756684AEBA4D0C0E4001F144CA154B91F53F53B085329E6ED9F573C7B6F", 16),
      lowerB = BigInteger("A17981033ED6094D9B6DF9AE7D75C956AC6221", 16),
      g = BigInteger("7"),
      v = BigInteger("56A9FAE94E75707FE301D86CF5EE5822B8EE642497E04A07BAA295C3A69A9652", 16),
      s = BigInteger("D7268532550D4750FC2C54667DADAD51D0CF8DC89C6468F54C4FE2B34C5A3FB3", 16),
      x = BigInteger.ZERO
    )
    val login = "ADMINISTRATOR"
    val actual = logonProofHandler.calculateProofResult(a, srp6Values, login)
    val expectedM = "4897559225CAE24FF536A8FEE59C95EE0C5BB6AC"
    val expectedK =
      "0DF89209C62950B748F09DB3FA9495B75A9B8F52CAA74FC646CF4926C3DDF75F68BE097CE53DD32B"
    assertThat(actual.m.toHexadecimalString()).isEqualTo(expectedM)
    assertThat(actual.k.toHexadecimalString()).isEqualTo(expectedK)
  }

  @Test
  fun testCalculateM2() {
    val sHash = "EE5772222579A1C3C61176E02B6EDF27111F2943538EAD8FB858842EAEF8FFDB"

    val vHash = "7FEE361FF13EA1DCF9C44F2252DAB77E86F056AB77306176859F2D352ECE52D4"

    val aByteArray = byteArrayOf(
      -72, -60, 42, 6, -78, -30, 55, -91, 29,
      110, 104, 2, 82, -48, -108, -90, -112,
      68, -99, 28, 118, -106, 6, 104, 120,
      -112, -98, 18, -52, -26, 69, 90
    )

    val k = byteArrayOf(
      -40, 38, 61, 117, 9, 67, -115, -36,
      -13, 124, 55, 92, -61, 7, -124, 32,
      -117, -74, -117, -95, -104, -74, 39,
      41, -124, 12, 25, -66, -110, 47, 63,
      -6, 81, -123, -111, 62, 113, 70, -50, 113
    )

    val upperB = byteArrayOf(
      -33, 66, 8, 42, -8, 27, -49, 71, 71, 83,
      121, -34, -128, 110, 12, -66, 124, 113,
      -95, 124, -88, -15, 74, 29, -104, -103,
      89, -23, -100, 25, 41, 114
    )

    val lowerB = byteArrayOf(
      -111, 116, 28, -21, 32, 64, -98, 84, 101,
      -80, 119, 101, 1, 70, -56, -93, -1, 35, -34
    )

    val m2 = byteArrayOf(
      123, 76, -3, -13, 17, 66, -90, 41, -14, -36,
      52, -20, 15, 70, -21, 114, 41, 100, 13, 16
    )

    val a = positiveBigInteger(aByteArray.reversedArray())
    val srp6Values = Srp6Values(
      upperB = positiveBigInteger(upperB.reversedArray()),
      lowerB = positiveBigInteger(lowerB.reversedArray()),
      g = BigInteger("7"),
      v = BigInteger(vHash, 16),
      s = BigInteger(sHash, 16),
      x = BigInteger.ZERO
    )
    val login = "ADMINISTRATOR"
    val actual = logonProofHandler.calculateProofResult(a, srp6Values, login)

    assertThat(actual.k.toReversedByteArray()).isEqualTo(k)
    assertThat(actual.m.toReversedByteArray()).isEqualTo(m2)
  }

  @Test
  fun testHandle() {
    val cmd: Byte = Command.AUTH_LOGON_PROOF.value
    val a = BigInteger("2E5C9E7353FA54BDA4B0A98E388562EA162AD6306D9FF2B57B8EFF6B61A63018", 16)
    val m1 = BigInteger("C43C54509253B48A2CBBDDF5092FF286F78E6A72", 16)
    val input = ByteBuffer.allocate(76).apply {
      put(cmd)
      put(a.toReversedByteArray())
      put(m1.toReversedByteArray())
      put(BigInteger(20 * 8, ThreadLocalRandom.current()).toReversedByteArray())
      put(0)
      put(0)
    }.array()

    val actual = logonProofHandler.handle(input, ClientHandlerMock)

    val expectedOutput = ByteBuffer.allocate(26).apply {
      put(Command.AUTH_LOGON_PROOF.value)
      put(0)
      put(
        byteArrayOf(
          85, 109, 84, -12, -83, -35, 123, -86, 34, -128,
          -58, -100, -111, -88, -81, 118, -18, -17, -44, 103
        )
      )
      put(0)
      put(0)
      put(0)
      put(0)
    }.array()

    assertThat(actual).isEqualTo(expectedOutput)
  }
}

private object AccountDbMock : AccountDb {
  override fun SQLQueryFactory.findSessionKey(username: String): String? = ""

  override fun SQLQueryFactory.update(username: String, body: SQLUpdateClause.() -> Unit): Long = 0L
  override fun SQLQueryFactory.findAccount(username: String): AccountBean? = accountDto
}

private object ClientHandlerMock : IClientHandler {
  override var sessionKey: String? = ""
  override var reconnectProof: BigInteger? = null
  override var srp6Values: Srp6Values? = Srp6Values(
    upperB = BigInteger("1BA953422EC758DD77DCB1FCC12A198E2CED7C6C2C7603EE575E2C8839954123", 16),
    lowerB = BigInteger("9837BC5E18F3A04F0638FF79987311767BE6D7", 16),
    v = BigInteger("7FEE361FF13EA1DCF9C44F2252DAB77E86F056AB77306176859F2D352ECE52D4", 16),
    s = BigInteger("EE5772222579A1C3C61176E02B6EDF27111F2943538EAD8FB858842EAEF8FFDB", 16),
    x = BigInteger.ZERO
  )
  override var login: String? = "ADMINISTRATOR"
  override val ip: String = "127.0.0.1"
}
