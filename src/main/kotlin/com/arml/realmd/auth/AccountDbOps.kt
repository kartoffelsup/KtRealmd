package com.arml.realmd.auth

import com.arml.realmd.DbOps
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upperCase

object AccountDbOps : AccountDb {
  override fun findAccount(username: String): AccountDto? {
    val result = DbOps.transaction {
      Account.select {
        Account.username.upperCase() eq username.toUpperCase()
      }.firstOrNull()
    }

    return result?.let {
      AccountDto(
        it[Account.id],
        it[Account.username],
        it[Account.shaPassHash],
        it[Account.gameMasterLevel],
        it[Account.sessionKey],
        it[Account.v],
        it[Account.s],
        it[Account.email],
        it[Account.joinDate],
        it[Account.lastIp],
        it[Account.failedLogins],
        it[Account.locked],
        it[Account.lastLogin],
        it[Account.activeRealmId],
        it[Account.expansion],
        it[Account.muteTime],
        it[Account.locale],
        it[Account.token]
      )
    }
  }

  override fun update(username: String, body: Account.(UpdateStatement) -> Unit) =
    DbOps.transaction {
      Account.update({ Account.username.upperCase() eq username.toUpperCase() }, body = body)
    }
}

interface AccountDb {
  fun findAccount(username: String): AccountDto?
  fun update(username: String, body: Account.(UpdateStatement) -> Unit): Int
}
