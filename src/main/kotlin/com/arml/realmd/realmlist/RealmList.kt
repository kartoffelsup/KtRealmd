package com.arml.realmd.realmlist

import org.jetbrains.exposed.sql.Table

object RealmList : Table(name = "realmlist") {
  val id = integer("id").primaryKey()
  val name = varchar("name", 32).uniqueIndex()
  val address = varchar("address", 32)
  val port = integer("port").default(8085)
  val icon = integer("icon").default(0)
  val realmFlags = integer("realmflags").default(2)
  val timezone = integer("timezone").default(0)
  val allowedSecurityLevel = integer("allowedSecurityLevel").default(0)
  val population = float("population").default(0.0f)
  val realmBuilds = varchar("realmbuilds", 64)
}

data class RealmListDto(
  val id: Int,
  val name: String,
  val address: String,
  val port: Int,
  val icon: Int,
  val realmFlags: Int,
  val timezone: Int,
  val allowedSecurityLevel: Int,
  val population: Float,
  val realmBuilds: String
)
