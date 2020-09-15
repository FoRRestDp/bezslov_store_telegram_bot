import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text

fun main() {
    bot {
        token = System.getenv("token")
        dispatch { 
            text { bot, update ->
                val text = update.message?.text ?: ""
                bot.sendMessage(chatId = update.message!!.chat.id, text = text.capitalize())
            }
        }
    }.startPolling()
}