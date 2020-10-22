package com.github.forrestdp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class CallbackCommand

@Serializable
@SerialName("showItems")
class ShowItemsCommand private constructor(val categoryId: Int, val pageNumber: Int) : CallbackCommand() {
    companion object {
        fun new(categoryId: Int, pageNumber: Int): CallbackCommand = ShowItemsCommand(categoryId, pageNumber)
    }
}

@Serializable
@SerialName("addItemToCart")
class AddItemToCartCommand private constructor(val itemId: Int) : CallbackCommand() {
    companion object {
        fun new(itemId: Int): CallbackCommand = AddItemToCartCommand(itemId)
    }
}

@Serializable
@SerialName("deleteItemFromCart")
class DeleteItemFromCartCommand private constructor(val itemId: Int, val itemIndex: Int) : CallbackCommand() {
    companion object {
        fun new(itemId: Int, itemIndex: Int): CallbackCommand = DeleteItemFromCartCommand(itemId, itemIndex)
    }
}

@Serializable
@SerialName("setItemCountInCart")
class SetItemCountInCartCommand private constructor(
    @SerialName("iid")
    val itemId: Int,
    @SerialName("iin")
    val itemIndex: Int,
    @SerialName("ic")
    val itemCount: Int,
) : CallbackCommand() {
    companion object {
        fun new(itemId: Int, itemIndex: Int, itemCount: Int): CallbackCommand =
            SetItemCountInCartCommand(itemId, itemIndex, itemCount)
    }
}

@Serializable
@SerialName("selectAnotherItem")
class SelectAnotherItemFromCartCommand private constructor(val itemIndex: Int) : CallbackCommand() {
    companion object {
        fun new(itemIndex: Int): CallbackCommand = SelectAnotherItemFromCartCommand(itemIndex)
    }
}

@Serializable
@SerialName("showCategories")
class ShowCategoriesCommand private constructor() : CallbackCommand() {
    companion object {
        fun new(): CallbackCommand = ShowCategoriesCommand()
    }
}

@Serializable
@SerialName("showCart")
class ShowCartCommand private constructor() : CallbackCommand() {
    companion object {
        fun new(): CallbackCommand = ShowCartCommand()
    }
}
