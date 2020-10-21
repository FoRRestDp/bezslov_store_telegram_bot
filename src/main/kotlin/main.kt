import com.github.forrestdp.dispatch
import com.github.forrestdp.tableentities.User
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
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
                password = password
        )
    } else {
        Database.connect(
                url = "jdbc:postgresql://localhost:5432/egorponomaryov",
                driver = "org.postgresql.Driver",
                user = "egorponomaryov",
                password = ""
        )
    }

    bot {
//        logLevel = HttpLoggingInterceptor.Level.NONE
        token = "1396640094:AAEHqmLePI56L_PPZt_OQZGbc8U9yWb94H0"
        dispatch { dispatch() }
    }.startPolling()
}