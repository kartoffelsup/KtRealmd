package com.arml.realmd.realmlist

import com.arml.realmd.auth.Account
import org.jetbrains.exposed.sql.Table

object RealmCharacters : Table(name = "realmcharacters") {
  val realmId = integer("realmid").primaryKey().references(RealmList.id)
  val accountId = integer("acctid").primaryKey().references(Account.id).index()
  val numChars = integer("numchars").default(0)
}

data class RealmCharactersDto(
  val realmId: Int,
  val accountId: Long,
  val numChars: Int
)
