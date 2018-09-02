package com.arml.realmd

import org.jetbrains.exposed.sql.Table

object RealmList : Table("realm_list") {
  val id = integer("id").autoIncrement().primaryKey()
  val name = varchar("name", 32).uniqueIndex()
  val address = varchar("address", 32).default("127.0.0.1")
  val port = integer("port").default(8085)
  val icon = integer("icon").default(0)
  val realmFlags = integer("realm_flags").default(2)
  val timezone = integer("timezone").default(0)
  val allowedSecurityLevel = integer("allowed_security_level").default(0)
  val population = float("population").default(0.0f)
  val realmBuilds = varchar("realm_builds", 64).default("")
}
