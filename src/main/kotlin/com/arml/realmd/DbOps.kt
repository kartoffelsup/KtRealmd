package com.arml.realmd

import com.arml.realmd.auth.Account
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

object DbOps {
  private val db by lazy {
    Database.connect(
      "jdbc:mysql://127.0.0.1/realmd",
      driver = "org.mariadb.jdbc.Driver",
      user = "realmd",
      password = "asdf"
    )
  }

  fun initializeSchema() {
    transaction(db) {
      SchemaUtils.create(RealmList, Account)
    }
  }

  fun <T> transaction(statement: Transaction.() -> T) = transaction(db, statement)
}
