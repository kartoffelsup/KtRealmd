package io.github.kartoffelsup.realmd.auth

import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.dml.SQLUpdateClause
import io.github.kartoffelsup.realmd.bean.AccountBean

interface AccountDb {
  fun SQLQueryFactory.findAccount(username: String): AccountBean?
  fun SQLQueryFactory.update(username: String, body: SQLUpdateClause.() -> Unit): Long
  fun SQLQueryFactory.findSessionKey(username: String): String?
}
