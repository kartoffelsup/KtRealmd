package com.arml.realmd

import com.arml.realmd.networking.ClientHandler
import com.xenomachina.argparser.ArgParser
import org.jetbrains.exposed.sql.Database
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ServerSocketFactory

fun main(args: Array<String>) {
  val realmdArgs = ArgParser(args).parseInto(::RealmdArgs)
  val config = ConfigParser(realmdArgs.configLocation, "ktrealmd").let(::Config)

  val db by lazy {
    Database.connect(
      url = "jdbc:mysql://${config.dbHost}:${config.dbPort}/${config.dbName}",
      driver = "org.mariadb.jdbc.Driver",
      user = config.dbUser,
      password = config.dbPass
    )
  }

  DbOps.initializeSchema(db)
  startListening(config)
}

private fun startListening(config: Config) {
  val host = config.listenAddress
  val port = config.listenPort
  val inetAddress = InetAddress.getByName(host)
  val serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 0, inetAddress)

  val executor: ExecutorService = Executors.newCachedThreadPool()
  while (!serverSocket.isClosed) {
    val client = try {
      serverSocket.accept()
    } catch (socketException: SocketException) {
      System.err.println("Socket Exception occurred during accept: $socketException")
      break
    }
    executor.execute(ClientHandler(client))
  }
}
