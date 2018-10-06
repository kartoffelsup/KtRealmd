package com.arml.realmd.networking

import com.arml.realmd.Command
import com.arml.realmd.CommandHandler
import com.arml.realmd.auth.AccountDbOps
import com.arml.realmd.auth.AuthResult
import com.arml.realmd.auth.Srp6Values
import com.arml.realmd.auth.challenge.LogonChallengeHandler
import com.arml.realmd.auth.challenge.ReconnectChallengeHandler
import com.arml.realmd.auth.proof.LogonProofHandler
import com.arml.realmd.auth.proof.ReconnectProofHandler
import com.arml.realmd.findCmd
import com.arml.realmd.realmlist.RealmListDbOps
import com.arml.realmd.realmlist.RealmListHandler
import java.io.IOException
import java.math.BigInteger
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class ClientHandler(
  private val client: Socket
) : Runnable, IClientHandler {
  private val running = AtomicBoolean(false)
  override var srp6Values: Srp6Values? = null
  override var login: String? = null
  override val ip: String = client.inetAddress.hostAddress
  override var sessionKey: String? = null
  override var reconnectProof: BigInteger? = null

  private val isConnected: Boolean
    get() = client.isConnected && !client.isClosed && running.get()

  init {
    this.running.set(true)
  }

  private fun sendMessage(payload: ByteArray) {
    ensureConnected()

    println("About to send ${payload.size} bytes")
    val clientOutputStream = client.getOutputStream()
    clientOutputStream.write(payload, 0, payload.size)
    clientOutputStream.flush()
  }

  override fun run() {
    val bytes = ByteArray(DEFAULT_PAYLOAD_LENGTH)
    try {
      while (running.get()) {
        val receivedBytes = client.getInputStream().read(bytes, 0, bytes.size)
        if (receivedBytes < 0) {
          System.err.println("Received <0 bytes")
          break
        }
        println("Received: $receivedBytes Byte(s) from $this ")
        val received = bytes.sliceArray(0..receivedBytes)
        val cmd = received[0]
        val command = findCmd(cmd)
        println("Received cmd: $command payload: ${received.contentToString()} from $client")
        val handler: CommandHandler? = command?.let(::handler)
        val response = handler?.handle(received, this) ?: byteArrayOf(
          cmd,
          AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.value,
          3,
          0
        )
        println("Sending ${response.contentToString()}")
        sendMessage(response)
      }

      client.close()
      println("$this disconnected.")
    } catch (ioe: IOException) {
      if (SOCKET_CLOSED.equals(ioe.message, ignoreCase = true)) {
        println(SOCKET_CLOSED + ioe)
        return
      }

      println("$this exception occurred: $ioe")
    }
  }

  private fun handler(cmd: Command): CommandHandler? = when (cmd) {
    Command.AUTH_LOGON_CHALLENGE -> LogonChallengeHandler(AccountDbOps)
    Command.AUTH_LOGON_PROOF -> LogonProofHandler(AccountDbOps)
    Command.REALM_LIST -> RealmListHandler(RealmListDbOps)
    Command.AUTH_RECONNECT_CHALLENGE -> ReconnectChallengeHandler(AccountDbOps)
    Command.AUTH_RECONNECT_PROOF -> ReconnectProofHandler
    else -> null
  }

  private fun ensureConnected() {
    check(isConnected) { "Not connected." }
  }

  companion object {
    private const val DEFAULT_PAYLOAD_LENGTH = 4096
    private const val SOCKET_CLOSED = "Socket closed"
  }
}
