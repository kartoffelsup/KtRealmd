package com.arml.realmd.realmlist

import com.arml.realmd.Command
import com.arml.realmd.CommandHandler
import com.arml.realmd.networking.IClientHandler
import com.google.common.primitives.Ints
import com.google.common.primitives.Shorts
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class RealmListHandler(
  private val realmListDb: RealmlistDb
) : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? {
    val realmToNumChars: List<Pair<RealmListDto, Int?>> =
      clientHandler.login?.let { realmListDb.findNumChars(it) } ?: listOf()
    val realmList = buildRealmList(realmToNumChars)
    val responseBuffer = ByteBuffer.allocate(3 + realmList.size)
      .apply {
        put(Command.REALM_LIST.value)
        put(Shorts.toByteArray(realmList.size.toShort()).reversedArray())
        put(realmList)
      }
    return responseBuffer.array()
  }

  private fun buildRealmList(realmToNumChars: List<Pair<RealmListDto, Int?>>): ByteArray {
    val realmListBuffer = ByteArrayOutputStream(128)
    realmListBuffer.run {
      write(byteArrayOf(0, 0, 0, 0))
      write(realmToNumChars.size)
      realmToNumChars.forEach { (realmList, numChars) ->
        write(Ints.toByteArray(realmList.icon).reversedArray())
        write(realmList.realmFlags)
        write(realmList.name.toByteArray(Charsets.UTF_8))
        write(0) // NUL BYTE to terminate string
        write("${realmList.address}:${realmList.port}".toByteArray(Charsets.UTF_8))
        write(0) // NUL BYTE to terminate stri
        write(ByteBuffer.allocate(4).putFloat(realmList.population).array())
        write(numChars ?: 0)
        write(realmList.timezone)
        write(0)
      }
      write(byteArrayOf(2, 0))
    }
    return realmListBuffer.toByteArray()
  }
}
