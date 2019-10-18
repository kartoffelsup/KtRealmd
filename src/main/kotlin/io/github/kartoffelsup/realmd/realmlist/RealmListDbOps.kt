package io.github.kartoffelsup.realmd.realmlist

import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.bean.RealmlistBean
import io.github.kartoffelsup.realmd.sql.QAccount.Companion.account
import io.github.kartoffelsup.realmd.sql.QRealmcharacters.Companion.realmcharacters
import io.github.kartoffelsup.realmd.sql.QRealmlist.Companion.realmlist

object RealmListDbOps : RealmListDb {
    override fun SQLQueryFactory.findNumChars(login: String): List<Pair<RealmlistBean, Int?>> =
        select(realmlist, realmcharacters.numchars)
            .from(realmlist)
            .innerJoin(account)
            .on(account.username.eq(login))
            .leftJoin(realmcharacters)
            .on(
                realmcharacters.acctid.eq(account.id).and(
                    realmlist.id.eq(realmcharacters.realmid)
                )
            )
            .distinct()
            .fetch()
            .mapNotNull { tuple ->
                val realmList = tuple.get(realmlist)
                realmList?.let {
                    it to tuple.get(realmcharacters.numchars)?.toInt()
                }
            }

}
