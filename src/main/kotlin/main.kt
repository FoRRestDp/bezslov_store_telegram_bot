import com.github.forrestdp.dispatch
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import org.jetbrains.exposed.sql.Database
import java.net.URI

fun main() {
    val databaseUrl: String? = System.getenv("DATABASE_URL")
    if (databaseUrl != null) {
        val dbUri = URI(databaseUrl)
        val username = dbUri.userInfo.split(":")[0]
        val password = dbUri.userInfo.split(":")[1]
        Database.connect(
            url = "jdbc:postgresql://${dbUri.host}:${dbUri.port}${dbUri.path}?sslmode=require",
            driver = "org.postgresql.Driver",
            user = username,
            password = password,
        )
    } else {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/egorponomaryov",
            driver = "org.postgresql.Driver",
            user = "user",
            password = "",
        )
    }

    bot {
        token = "token"
        dispatch { dispatch() }
    }.startPolling()
}
