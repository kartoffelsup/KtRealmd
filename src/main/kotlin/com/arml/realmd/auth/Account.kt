package com.arml.realmd.auth

import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

object Account : Table(name = "account") {
  val id = integer("id").autoIncrement().primaryKey()
  val username = varchar("username", 32)
  val shaPassHash = varchar("sha_pass_hash", 40)
  val gameMasterLevel = integer("gmlevel").default(0)
  val sessionKey = text("sessionkey")
  val v = text("v").nullable()
  val s = text("s").nullable()
  val email = text("email")
  val joinDate = datetime("joindate").default(DateTime.now())
  val lastIp = varchar("last_ip", 30).default("0.0.0.0")
  val failedLogins = integer("failed_logins").default(0)
  val locked = bool("locked").default(false)
  val lastLogin = datetime("last_login").nullable()
  val activeRealmId = integer("active_realm_id").default(0)
  val expansion = integer("expansion").default(0)
  val muteTime = long("mutetime").default(0L)
  val locale = integer("locale").default(0)
  val token = text("token").nullable()
}

data class AccountDto(
  val id: Int,
  val username: String,
  val shaPassHash: String,
  val gameMasterLevel: Int,
  val sessionKey: String,
  val v: String?,
  val s: String?,
  val email: String?,
  val joinDate: DateTime,
  val lastIp: String,
  val failedLogins: Int,
  val locked: Boolean,
  val lastLogin: DateTime?,
  val activeRealmId: Int,
  val expansion: Int,
  val muteTime: Long,
  val locale: Int,
  val token: String?
)
