package com.arml.realmd.auth.proof

import com.arml.realmd.Command
import com.arml.realmd.CommandHandler
import com.arml.realmd.auth.AuthResult
import com.arml.realmd.networking.IClientHandler
import com.arml.realmd.util.hexStringToByteArray
import com.arml.realmd.util.stripLeadingZeros
import com.arml.realmd.util.toReversedByteArray

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
