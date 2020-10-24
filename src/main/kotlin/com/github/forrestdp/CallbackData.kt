package com.github.forrestdp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class CallbackData

fun CallbackData.toJsonString() = Json.encodeToString(this)

@Serializable
@SerialName("showItems")
class ShowItemsCallbackData private constructor(val categoryId: Int, val pageNumber: Int) : CallbackData() {
    companion object {
        fun new(categoryId: Int, pageNumber: Int): CallbackData = ShowItemsCallbackData(categoryId, pageNumber)
    }
}

@Serializable
@SerialName("setItemCountInList")
class SetItemCountInListCallbackData private constructor(
    @SerialName("ii") val itemId: Int,
    @SerialName("ic") val itemCountInList: Int,
) : CallbackData() {
    companion object {
        fun new(itemId: Int, itemCountInList: Int): CallbackData
                = SetItemCountInListCallbackData(itemId, itemCountInList)
    }
}

@Serializable
@SerialName("deleteItemFromCart")
class DeleteItemFromCartCallbackData private constructor(val itemId: Int, val itemIndex: Int) : CallbackData() {
    companion object {
        fun new(itemId: Int, itemIndex: Int): CallbackData = DeleteItemFromCartCallbackData(itemId, itemIndex)
    }
}

@Serializable
@SerialName("setItemCountInCart")
class SetItemCountInCartCallbackData private constructor(
    @SerialName("iid") val itemId: Int,
    @SerialName("ic") val itemCountInCart: Int,
    @SerialName("iin") val itemIndexInCart: Int,
) : CallbackData() {
    companion object {
        fun new(itemId: Int, itemIndex: Int, itemCount: Int): CallbackData =
            SetItemCountInCartCallbackData(itemId, itemCount, itemIndex)
    }
}

@Serializable
@SerialName("selectAnotherItem")
class SetItemIndexInCartCallbackData private constructor(val itemIndex: Int) : CallbackData() {
    companion object {
        fun new(itemIndex: Int): CallbackData = SetItemIndexInCartCallbackData(itemIndex)
    }
}

@Serializable
@SerialName("showCategories")
class ShowCategoriesCallbackData private constructor() : CallbackData() {
    companion object {
        fun new(): CallbackData = ShowCategoriesCallbackData()
    }
}

@Serializable
@SerialName("showCart")
class ShowCartCallbackData private constructor() : CallbackData() {
    companion object {
        fun new(): CallbackData = ShowCartCallbackData()
    }
}

@Serializable
@SerialName("noAction")
class NoActionCallbackData private constructor() : CallbackData() {
    companion object {
        fun new(): CallbackData = NoActionCallbackData()
    }
}

@Serializable
@SerialName("checkout")
class CheckoutCallbackData private constructor() : CallbackData() {
    companion object {
        fun new(): CallbackData = CheckoutCallbackData()
    }
}
