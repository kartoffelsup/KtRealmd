package io.github.kartoffelsup.realmd

import com.querydsl.sql.Configuration
import com.querydsl.sql.MySQLTemplates
import com.querydsl.sql.SQLQueryFactory
import com.xenomachina.argparser.ArgParser
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.kartoffelsup.realmd.networking.ClientHandler
import org.mariadb.jdbc.Driver
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ServerSocketFactory

fun main(args: Array<String>) {
    val realmdArgs = ArgParser(args).parseInto(::RealmdArgs)
    val config = ConfigParser(realmdArgs.configLocation, "ktrealmd").let(::Config)

    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:mariadb://${config.dbHost}:${config.dbPort}/${config.dbName}"
        driverClassName = Driver::class.java.name
        username = config.dbUser
        password = config.dbPass
    })

    val sqlQueryFactory = SQLQueryFactory(Configuration(MySQLTemplates()), dataSource)
    startListening(config, sqlQueryFactory)
}

private fun startListening(config: Config, sqlQueryFactory: SQLQueryFactory) {
    val host = config.listenAddress
    val port = config.listenPort
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
