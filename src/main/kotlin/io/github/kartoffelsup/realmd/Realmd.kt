package io.github.kartoffelsup.realmd

import com.querydsl.sql.Configuration
import com.querydsl.sql.MySQLTemplates
import com.querydsl.sql.SQLQueryFactory
import com.sksamuel.hoplite.ConfigLoader
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.kartoffelsup.realmd.networking.ClientHandler
import org.mariadb.jdbc.Driver
import java.net.InetAddress
import java.net.SocketException
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ServerSocketFactory

fun main(args: Array<String>) {
    val configFile: String = args.first()
    val config = ConfigLoader().loadConfigOrThrow<Config>(Paths.get(configFile))

    val dataSource = HikariDataSource(HikariConfig().apply {
        val db = config.db
        jdbcUrl = "jdbc:mariadb://${db.host}:${db.port}/${db.name}"
        driverClassName = Driver::class.java.name
        username = db.user
        password = db.pass
    })

    val sqlQueryFactory = SQLQueryFactory(Configuration(MySQLTemplates()), dataSource)
    startListening(config.listener, sqlQueryFactory)
}

private fun startListening(listener: Config.Listener, sqlQueryFactory: SQLQueryFactory) {
    val host = listener.address
    val port = listener.port
    val inetAddress = InetAddress.getByName(host)
    val serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 0, inetAddress)
    println("Listening on $host:$port")
    val executor: ExecutorService = Executors.newCachedThreadPool()
    while (!serverSocket.isClosed) {
        val client = try {
            serverSocket.accept()
        } catch (socketException: SocketException) {
            System.err.println("Socket Exception occurred during accept: $socketException")
            break
        }
        executor.execute(ClientHandler(client, sqlQueryFactory))
    }
}
