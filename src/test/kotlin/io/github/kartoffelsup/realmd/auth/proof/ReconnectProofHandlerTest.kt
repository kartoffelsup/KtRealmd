package io.github.kartoffelsup.realmd.auth.proof

import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.auth.AuthResult
import io.github.kartoffelsup.realmd.auth.Srp6Values
import io.github.kartoffelsup.realmd.networking.IClientHandler
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ReconnectProofHandlerTest {

  @Test
  fun handle() {
    val input = byteArrayOf(
      3, 4, 60, -109, -123, -11, -57, 8, 115, 97, 114, -50, 101, 43, 99,
      -32, -11, -51, -115, -16, 58, 48, 52, -21, -73, -73, -117, 109, -97, 94, 12, -59, -71, -96,
      33, 90, -17, 45, 94, 36, -21, 7, 61, 59, -120, -52, 42, -80, -76, 72, 87, -8, -13, 101, 103,
      -11, -95, 0, 0
    )

    val response = ReconnectProofHandler.handle(input, ClientMock)
    assertThat(response).isEqualTo(
      byteArrayOf(
        Command.AUTH_RECONNECT_PROOF.value,
        AuthResult.WOW_SUCCESS.value,
        0,
        0
      )
    )
  }

  object ClientMock : IClientHandler {
    override var srp6Values: Srp6Values? = null
    override var login: String? = "TEST"
    override val ip: String = "127.0.0.1"
    override var sessionKey: String? =
      "922C3612CC74C134CC98BC60A6DAEACAC9F99230F05ADC68E38EFEA616ABD06ACA08F383778DE3CC"
    override var reconnectProof: BigInteger? = BigInteger("30565386821389188428176182010392147228")
  }
}
