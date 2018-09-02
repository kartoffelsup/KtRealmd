package com.arml.realmd

import com.arml.realmd.networking.ClientHandler

interface CommandHandler {
  fun handle(input: ByteArray, clientHandler: ClientHandler): ByteArray?
}
