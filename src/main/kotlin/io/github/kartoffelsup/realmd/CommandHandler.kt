package io.github.kartoffelsup.realmd

import io.github.kartoffelsup.realmd.networking.IClientHandler

interface CommandHandler {
    fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray?
}
