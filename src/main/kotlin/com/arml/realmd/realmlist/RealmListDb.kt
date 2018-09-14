package com.arml.realmd.realmlist

interface RealmListDb {
  fun findNumChars(login: String): List<Pair<RealmListDto, Int?>>
}
