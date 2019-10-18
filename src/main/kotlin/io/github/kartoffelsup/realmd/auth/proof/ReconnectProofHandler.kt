package io.github.kartoffelsup.realmd.auth.proof

import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.CommandHandler
import io.github.kartoffelsup.realmd.auth.AuthResult
import io.github.kartoffelsup.realmd.networking.IClientHandler
import io.github.kartoffelsup.realmd.util.hexStringToByteArray
import io.github.kartoffelsup.realmd.util.stripLeadingZeros
import io.github.kartoffelsup.realmd.util.toReversedByteArray

object ReconnectProofHandler : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? {
    val reconnectProofOpt = ReconnectProofParser.parse(input)
    val login = clientHandler.login ?: return null
    val reconnectProof = clientHandler.reconnectProof ?: return null
    val sessionKey = clientHandler.sessionKey ?: return null
    return reconnectProofOpt?.let { reconProofParams ->
      val sha = sha1 {
        update(login.toByteArray())
        update(reconProofParams.r1.toReversedByteArray())
        update(reconnectProof.toReversedByteArray())
        update(sessionKey.hexStringToByteArray().reversedArray())
      }

      if (sha.contentEquals(reconProofParams.r2.toByteArray().stripLeadingZeros())) {
        byteArrayOf(
          Command.AUTH_RECONNECT_PROOF.value,
          AuthResult.WOW_SUCCESS.value,
          0,
          0
        )
      } else {
        null
      }
    }
  }
}
