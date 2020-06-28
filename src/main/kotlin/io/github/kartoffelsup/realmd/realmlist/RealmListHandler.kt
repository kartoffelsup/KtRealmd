package io.github.kartoffelsup.realmd.realmlist

import io.github.kartoffelsup.realmd.Command
import io.github.kartoffelsup.realmd.CommandHandler
import io.github.kartoffelsup.realmd.networking.IClientHandler
import com.google.common.primitives.Ints
import com.google.common.primitives.Shorts
import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.bean.RealmlistBean
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class RealmListHandler(
    private val realmListDb: RealmListDb,
    private val sqlQueryFactory: SQLQueryFactory
) : CommandHandler {
  override fun handle(input: ByteArray, clientHandler: IClientHandler): ByteArray? = realmListDb.run {
    val realmToNumChars: List<Pair<RealmlistBean, Int?>> =
      clientHandler.login?.let { sqlQueryFactory.findNumChars(it) } ?: listOf()
    val realmList = buildRealmList(realmToNumChars)
    val responseBuffer = ByteBuffer.allocate(3 + realmList.size)
      .apply {
        put(Command.REALM_LIST.value)
        put(Shorts.toByteArray(realmList.size.toShort()).reversedArray())
        put(realmList)
      }
    responseBuffer.array()
  }

  private fun buildRealmList(realmToNumChars: List<Pair<RealmlistBean, Int?>>): ByteArray {
    val realmListBuffer = ByteArrayOutputStream(128)
    realmListBuffer.run {
      write(byteArrayOf(0, 0, 0, 0))
      write(realmToNumChars.size)
      realmToNumChars.forEach { (realmList, numChars) ->
        write(Ints.toByteArray(realmList.icon.toInt()).reversedArray())
        write(realmList.realmflags.toInt())
        write(realmList.name.toByteArray(Charsets.UTF_8))
        write(0) // NUL BYTE to terminate string
        write("${realmList.address}:${realmList.port}".toByteArray(Charsets.UTF_8))
        write(0) // NUL BYTE to terminate string
        write(ByteBuffer.allocate(4).putFloat(realmList.population).array())
        write(numChars ?: 0)
        write(realmList.timezone.toInt())
        write(0)
      }
      write(byteArrayOf(2, 0))
    }
    return realmListBuffer.toByteArray()
  }
}
