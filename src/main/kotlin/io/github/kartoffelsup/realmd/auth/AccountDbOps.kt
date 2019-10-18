package io.github.kartoffelsup.realmd.auth

import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.dml.SQLUpdateClause
import io.github.kartoffelsup.realmd.bean.AccountBean
import io.github.kartoffelsup.realmd.sql.QAccount

object AccountDbOps : AccountDb {
    override fun SQLQueryFactory.findSessionKey(username: String): String? =
        select(QAccount.account.sessionkey)
            .from(QAccount.account)
            .where(QAccount.account.username.eq(username))
            .fetch()
            .firstOrNull()

    override fun SQLQueryFactory.findAccount(username: String): AccountBean? =
        select(QAccount.account)
            .from(QAccount.account)
            .where(QAccount.account.username.toUpperCase().eq(username.toUpperCase()))
            .fetch()
            .firstOrNull()


    override fun SQLQueryFactory.update(username: String, body: SQLUpdateClause.() -> Unit): Long {
        val updateQuery = update(QAccount.account)
            .where(QAccount.account.username.equalsIgnoreCase(username))
        body(updateQuery)
        return updateQuery
            .execute()
    }
}
