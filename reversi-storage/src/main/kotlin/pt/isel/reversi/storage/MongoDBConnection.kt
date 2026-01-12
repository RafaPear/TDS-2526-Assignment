package pt.isel.reversi.storage

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class MongoDBConnection(
    val user: String,
    val password: String,
    val host: String,
    val port: Int
) {

    init {
        require(user.isNotBlank())
        require(password.isNotBlank())
        require(host.isNotBlank())
        require(port in 1..65535)
    }

    fun getConnectionString(): String {
        val u = URLEncoder.encode(user, StandardCharsets.UTF_8)
        val p = URLEncoder.encode(password, StandardCharsets.UTF_8)

        return "mongodb://$u:$p@$host:$port"
    }
}
