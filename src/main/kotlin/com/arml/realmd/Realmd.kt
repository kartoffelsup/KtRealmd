package com.arml.realmd

import com.arml.realmd.networking.ClientHandler
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ServerSocketFactory


fun main(args: Array<String>) {
  DbOps.initializeSchema()

  val host = "127.0.0.1"
  val port = 3724
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
