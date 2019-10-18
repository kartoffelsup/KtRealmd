package io.github.kartoffelsup.realmd.realmlist

import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.realmd.bean.RealmlistBean
import io.github.kartoffelsup.realmd.sql.QAccount
import io.github.kartoffelsup.realmd.sql.QRealmcharacters
import io.github.kartoffelsup.realmd.sql.QRealmlist

object RealmListDbOps : RealmListDb {
    override fun SQLQueryFactory.findNumChars(login: String): List<Pair<RealmlistBean, Int?>> {
        return select(QRealmlist.realmlist, QRealmcharacters.realmcharacters.numchars)
            .from(QRealmlist.realmlist)
            .innerJoin(QAccount.account)
            .on(QAccount.account.username.eq(login))
            .leftJoin(QRealmcharacters.realmcharacters)
            .on(
                QRealmcharacters.realmcharacters.acctid.castToNum(Int::class.java).eq(QAccount.account.id)
                    .and(QRealmlist.realmlist.id.eq(QRealmcharacters.realmcharacters.realmid))
            )
            .distinct()
            .fetch()
            .mapNotNull { tuple ->
                val realmList = tuple.get(QRealmlist.realmlist)
                realmList?.let {
                    it to tuple.get(QRealmcharacters.realmcharacters.numchars)?.toInt()
                }
            }
    }
}
