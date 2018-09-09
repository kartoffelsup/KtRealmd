package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import com.arml.realmd.auth.Account
import com.arml.realmd.auth.AccountDb
import com.arml.realmd.auth.AccountDto
import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.auth.accountDto
import com.arml.realmd.networking.IClientHandler
import com.arml.realmd.util.positiveBigInteger
import com.arml.realmd.util.toHexadecimalString
import com.arml.realmd.util.toReversedByteArray
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.concurrent.ThreadLocalRandom

@ExperimentalUnsignedTypes
class LogonProofHandlerTest {

  private val logonProofHandler = LogonProofHandler(AccountDbMock)

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

    val aByteArray = ubyteArrayOf(
      184, 196, 42, 6, 178, 226, 55,
      165, 29, 110, 104, 2, 82, 208, 148,
      166, 144, 68, 157, 28, 118,
      150, 6, 104, 120, 144, 158,
      18, 204, 230, 69, 90
    ).toByteArray()

    val k = ubyteArrayOf(
      216, 38, 61, 117, 9, 67,
      141, 220, 243, 124, 55, 92,
      195, 7, 132, 32, 139, 182, 139,
      161, 152, 182, 39, 41, 132,
      12, 25, 190, 146, 47, 63,
      250, 81, 133, 145, 62,
      113, 70, 206, 113
    ).toByteArray()

    val upperB = ubyteArrayOf(
      223, 66, 8, 42, 248, 27, 207,
      71, 71, 83, 121, 222, 128, 110,
      12, 190, 124, 113, 161, 124,
      168, 241, 74, 29, 152, 153,
      89, 233, 156, 25, 41, 114
    ).toByteArray()

    val lowerB = ubyteArrayOf(
      145, 116, 28, 235, 32, 64, 158, 84, 101,
      176, 119, 101, 1, 70, 200, 163,
      255, 35, 222
    ).toByteArray()

    val m2 = ubyteArrayOf(
      123, 76, 253, 243, 17, 66, 166, 41, 242, 220, 52,
      236, 15, 70, 235, 114, 41, 100, 13, 16
    ).toByteArray()

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
        ubyteArrayOf(
          85, 109, 84, 244, 173,
          221, 123, 170, 34, 128,
          198, 156, 145, 168, 175,
          118, 238, 239, 212, 103
        ).toByteArray()
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
  override fun update(username: String, body: Account.(UpdateStatement) -> Unit): Int = 0
  override fun findAccount(username: String): AccountDto? = accountDto
}

private object ClientHandlerMock : IClientHandler {
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
