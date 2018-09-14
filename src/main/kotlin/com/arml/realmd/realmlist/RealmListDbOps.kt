package com.arml.realmd.realmlist

import com.arml.realmd.DbOps
import com.arml.realmd.auth.Account
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll

object RealmListDbOps : RealmListDb {
  override fun findNumChars(login: String): List<Pair<RealmListDto, Int?>> {
    return DbOps.transaction {
      RealmList.join(Account, JoinType.LEFT, additionalConstraint = { Account.username eq login })
        .leftJoin(RealmCharacters)
        .slice(
          RealmList.columns + RealmCharacters.numChars
        )
        .selectAll()
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
