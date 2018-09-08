package com.arml.realmd

import com.arml.realmd.networking.IClientHandler

interface CommandHandler {
  fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray?
}
