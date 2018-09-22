package com.arml.realmd

import com.arml.realmd.auth.Account
import com.arml.realmd.realmlist.RealmCharacters
import com.arml.realmd.realmlist.RealmList
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DbOps {
  fun initializeSchema(db: Database) {
    transaction(db) {
      SchemaUtils.create(Account, RealmList, RealmCharacters)
    }
  }
}
