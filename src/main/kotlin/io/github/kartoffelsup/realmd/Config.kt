package io.github.kartoffelsup.realmd

data class Config(
    val db: Db,
    val listener: Listener
) {
    data class Db(
        val host: String,
        val port: Int,
        val pass: String,
        val user: String,
        val name: String
    )

    data class Listener(
        val address: String,
        val port: Int
    )
}
