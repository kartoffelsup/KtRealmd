package io.github.kartoffelsup.realmd.realmlist

import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.bean.RealmlistBean

interface RealmListDb {
  fun SQLQueryFactory.findNumChars(login: String): List<Pair<RealmlistBean, Int?>>
}
