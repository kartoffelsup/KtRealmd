package io.github.kartoffelsup.realmd.networking

import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.CommandHandler
import io.github.kartoffelsup.realmd.auth.AccountDbOps
import io.github.kartoffelsup.realmd.auth.AuthResult
import io.github.kartoffelsup.realmd.auth.Srp6Values
import io.github.kartoffelsup.realmd.auth.challenge.LogonChallengeHandler
import io.github.kartoffelsup.realmd.auth.challenge.ReconnectChallengeHandler
import io.github.kartoffelsup.realmd.auth.proof.LogonProofHandler
import io.github.kartoffelsup.realmd.auth.proof.ReconnectProofHandler
import io.github.kartoffelsup.realmd.findCmd
import io.github.kartoffelsup.realmd.realmlist.RealmListDbOps
import io.github.kartoffelsup.realmd.realmlist.RealmListHandler
import java.io.IOException
import java.math.BigInteger
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class ClientHandler(
    private val client: Socket,
    private val sqlQueryFactory: SQLQueryFactory
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
        Command.AUTH_LOGON_CHALLENGE -> LogonChallengeHandler(AccountDbOps, sqlQueryFactory)
        Command.AUTH_LOGON_PROOF -> LogonProofHandler(AccountDbOps, sqlQueryFactory)
        Command.REALM_LIST -> RealmListHandler(RealmListDbOps, sqlQueryFactory)
        Command.AUTH_RECONNECT_CHALLENGE -> ReconnectChallengeHandler(AccountDbOps, sqlQueryFactory)
        Command.AUTH_RECONNECT_PROOF -> ReconnectProofHandler
        else -> null
    }

    private fun ensureConnected() {
        check(isConnected) { "Not connected." }
    }

    override fun toString(): String {
        return "ClientHandler(ip='$ip',login='$login')"
    }

    companion object {
        private const val DEFAULT_PAYLOAD_LENGTH = 4096
        private const val SOCKET_CLOSED = "Socket closed"
    }
}
