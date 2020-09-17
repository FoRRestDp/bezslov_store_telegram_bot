import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import com.github.kotlintelegrambot.entities.KeyboardButton

object Categories : IntIdTable() {
    override val primaryKey = PrimaryKey(id)
    val name: Column<String> = varchar("name", 20)
    val hide: Column<Boolean> = bool("hide").default(false)
}

object Items : IntIdTable() {
    override val primaryKey = PrimaryKey(id)
    val categoryId: Column<Int> = integer("category").references(Categories.id)
    val name: Column<String> = varchar("name", 200)
    val description: Column<String?> = text("description").nullable()
    val imageTelegram: Column<String?> = varchar("image_tlg", 200).nullable()
    val img: Column<String?> = varchar("img", 200).nullable()
    val price: Column<BigDecimal?> = decimal("price", 9, 2).nullable()
    val hide: Column<Boolean> = bool("hide").default(false)
}

object Users : Table() {
    val chatId: Column<Long> = long("chat_id")
    override val primaryKey = PrimaryKey(Users.chatId)
    val firstName: Column<String?> = varchar("first_name", 32).nullable()
    val lastName: Column<String?> = varchar("last_name", 32).nullable()
    val phone: Column<String?> = varchar("phone", 15).nullable()
    val address: Column<String?> = varchar("address", 255).nullable()
    val action: Column<String?> = varchar("action", 20).nullable()
}

object UsersItems : IntIdTable("users_items") {
    override val primaryKey = PrimaryKey(id)
    val itemId = integer("item_id").references(Items.id)
    val itemCount = integer("item_count").default(1)
    val userChatId = long("user_chat_id").references(Users.chatId)
}

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
    val db = Database.connect(
            url = "jdbc:postgresql://localhost:5432/egorponomaryov",
            driver = "org.postgresql.Driver",
            user = "egorponomaryov",
            password = ""
    )

    bot {
        token = "1396640094:AAEHqmLePI56L_PPZt_OQZGbc8U9yWb94H0"
        dispatch {
            command("start") { bot, update ->
                val chatId = update.message!!.chat.id
                transaction {
                    Users.insertIgnore {
                        it[this.chatId] = chatId
                    }
                }
                val showItemsButton = KeyboardButton("Просмотр товаров")
                val goToCartButton = KeyboardButton("Корзина")
                val checkoutButton = KeyboardButton("Оформить заказ")
                val mainKeyboardMarkup = KeyboardReplyMarkup(showItemsButton, goToCartButton, checkoutButton, resizeKeyboard = true)
                bot.sendMessage(
                        chatId,
                        """
                        Добро пожаловать в магазин "Без слов"
                        """.trimIndent(),
                        replyMarkup = mainKeyboardMarkup
                )
            }
            text("Просмотр товаров") { bot, update ->
                val chatId = update.message?.chat?.id ?: throw Exception("Message not defined")
                transaction {
                    val categoriesQuery = Categories
                            .select { Categories.hide eq Op.FALSE }
                    if (categoriesQuery.count() == 1L) {
                        val itemsQuery = Items.select { Items.hide eq Op.FALSE }
                        for (entry in itemsQuery) {
                            val category = Categories
                                    .slice(Categories.name)
                                    .select { Categories.id eq entry[Items.categoryId] }
                                    .firstOrNull()
                                    ?.get(Categories.name) ?: "неизвестно какой"
                            val id = entry[Items.id]
                            val name = entry[Items.name]
                            val description = entry[Items.description] ?: "его пока нет"
                            val imageTelegram = entry[Items.imageTelegram]
                            val price = entry[Items.price]?.toString() ?: "пока неизвестна"
                            val responseText = """
                                $name
                                (В категории: $category)
                                Описание: $description
                                Цена: $price
                                
                            """.trimIndent()
                            val imk = InlineKeyboardMarkup(listOf(listOf(InlineKeyboardButton("В корзину", callbackData = "addItemToCart_$id"))))
                            bot.sendMessage(chatId, responseText, replyMarkup = imk)
                        }
                    } else {
                        bot.sendMessage(chatId, "Пока не поддерживается")
                    }
                }
            }
            text("Корзина") { bot, update ->
                val chatId = update.message?.chat?.id ?: throw Exception("Message is not defined")
                transaction {
                    (UsersItems innerJoin  Items)
                            .slice(Items.name, Items.price, UsersItems.itemCount)
                            .select { (UsersItems.itemId eq Items.id) and (UsersItems.userChatId eq chatId) }
                            .forEach {
                                bot.sendMessage(chatId,
                                text = """
                                    ${it[Items.name]}
                                    Количество: ${it[UsersItems.itemCount]}
                                    Цена одной шт.: ${it[Items.price] ?: "неизвестна"}
                                """.trimIndent())
                            }
                }
            }
            text("Оформить заказ") { bot, update -> 
                val chatId = update.message?.chat?.id ?: throw Exception("Message is not defined")
                bot.sendMessage(chatId, "Пока недоступно")
            }
            callbackQuery { bot, update ->
                val chatId: Long = update.callbackQuery?.message?.chat?.id
                        ?: throw Exception("Callback query or it's message is not defined")
                val data = update.callbackQuery?.data ?: ""
                when {
                    data.startsWith("addItemToCart") -> {
                        val selectedItemId = data.split("_")[1].toIntOrNull()
                                ?: throw Exception("Product id is not defined")
                        transaction {
                            val selectedItemCount = UsersItems
                                    .slice(UsersItems.itemCount)
                                    .select { (UsersItems.itemId eq selectedItemId) and (UsersItems.userChatId eq chatId) }
                                    .firstOrNull()
                                    ?.getOrNull(UsersItems.itemCount) ?: 0
                            if (selectedItemCount == 0) {
                                UsersItems.insert {
                                    it[itemId] = selectedItemId
                                    it[userChatId] = chatId
                                }
                            } else {
                                UsersItems.update({ (UsersItems.itemId eq selectedItemId) and (UsersItems.userChatId eq chatId) }) {
                                    it[itemCount] = selectedItemCount + 1
                                }
                            }
                        }
                    }
                }
            }
            /*command("admin") { bot, update ->
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
            *//*text { bot, update ->
                when (update.adminStep) {
                    AdminStep.AddCategory -> TODO()
                    AdminStep.AddProduct.Step1 -> TODO()
                    AdminStep.AddProduct.Step2 -> TODO()
                    AdminStep.AddProduct.Step3 -> TODO()
                    AdminStep.AddProduct.Step4 -> TODO()
                    AdminStep.AddContact -> TODO()
                    else -> TODO()
                }
            }*/
            /*photos { bot, update, list ->
                if (!update.isUserAdmin) {
                    return@photos
                }
                when (update.adminStep) {
                    AdminStep.AddProduct.Step5 -> TODO()
                    else -> TODO()
                }
            }*/
        }
    }.startPolling()
}