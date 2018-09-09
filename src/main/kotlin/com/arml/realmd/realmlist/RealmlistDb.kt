package com.arml.realmd.realmlist

interface RealmlistDb {
  fun findNumChars(login: String): List<Pair<RealmListDto, Int?>>
}
