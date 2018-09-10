package com.arml.realmd.realmlist

import com.arml.realmd.DbOps
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select

object RealmlistDbOps : RealmlistDb {
  override fun findNumChars(login: String): List<Pair<RealmListDto, Int?>> {
    return DbOps.transaction {
      (RealmList leftJoin  RealmCharacters)
        .select { (RealmList.id eq RealmCharacters.realmId) or RealmCharacters.realmId.isNull()}
        .distinctBy { it[RealmList.id] }
        .map {
          RealmListDto(
            it[RealmList.id],
            it[RealmList.name],
            it[RealmList.address],
            it[RealmList.port],
            it[RealmList.icon],
            it[RealmList.realmFlags],
            it[RealmList.timezone],
            it[RealmList.allowedSecurityLevel],
            it[RealmList.population],
            it[RealmList.realmBuilds]
          ) to
            it[RealmCharacters.numChars]
        }
    }
  }
}
