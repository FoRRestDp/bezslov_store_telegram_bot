import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.photos
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.Database

val Update.isUserAdmin: Boolean
    get() = message!!.chat.username == ADMIN_CHAT_ID

val Update.adminStep: AdminStep
    get() = TODO()

const val ADMIN_CHAT_ID = "FoRRestDp"
const val WRONG_COMMAND_RESPONSE = "Неверная команда"

sealed class AdminStep {
    object AddCategory : AdminStep()
    sealed class AddProduct : AdminStep() {
        object Step1 : AddProduct()
        object Step2 : AddProduct()
        object Step3 : AddProduct()
        object Step4 : AddProduct()
        object Step5 : AddProduct()
    }

    object AddContact : AdminStep()
}

fun main() {
    /*val dbUri = runCatching {  URI(System.getenv("DATABASE_URL")) }.getOrElse { URI("jdbc:postgres://egorponomaryov") }

    val username: String = runCatching { dbUri.userInfo.split(":")[0] }.getOrElse { "egorponomaryov" }
    val password: String = dbUri.userInfo.split(":")[1]
    val dbUrl =
        "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path.toString() + "?sslmode=require"*/
    /*val db = Database.connect(
            url = "jdbc:postgresql://localhost:5432/egorponomaryov",
            driver = "org.postgresql.Driver",
            user = "egorponomaryov",
            password = ""
    )*/

    bot {
        token = System.getenv("token")
        dispatch {
            command("start") { bot, update ->
                val chatId = update.message!!.chat.id
                val button = InlineKeyboardButton(text = "К покупкам", callbackData = "showProducts")
                val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(listOf(button)))
                bot.sendMessage(
                        chatId,
                        """
                        Добро пожаловать в магазин "Без слов"
                        """.trimIndent(),
                        replyMarkup = inlineKeyboardMarkup
                )
            }
            command("admin") { bot, update ->
                val chatId = update.message!!.chat.id
                if (update.isUserAdmin) {
                    bot.sendMessage(chatId, """
                        Администрирование
                        /editcategories - Изменение категорий товаров
                        /editcontacts - Изменение контактов
                    """.trimIndent())
                } else {
                    bot.sendMessage(chatId, WRONG_COMMAND_RESPONSE)
                }
            }
            command("editcategories") { bot, update ->
                // DELETE FROM bot_shop_product_temp"
                // DELETE FROM bot_shop_action_admin

            }
            command("addcontact") { bot, update -> TODO() }
            command("editcontacts") { bot, update -> TODO() }
            text { bot, update ->
                when (update.adminStep) {
                    AdminStep.AddCategory -> TODO()
                    AdminStep.AddProduct.Step1 -> TODO()
                    AdminStep.AddProduct.Step2 -> TODO()
                    AdminStep.AddProduct.Step3 -> TODO()
                    AdminStep.AddProduct.Step4 -> TODO()
                    AdminStep.AddContact -> TODO()
                    else -> TODO()
                }
            }
            photos { bot, update, list ->
                if (!update.isUserAdmin) {
                    return@photos
                }
                when (update.adminStep) {
                    AdminStep.AddProduct.Step5 -> TODO()
                    else -> TODO()
                }
            }
        }
    }.startPolling()
}