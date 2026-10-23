package com.apps.iatinyfish.data.remote

import com.apps.iatinyfish.data.model.ProductOffer
import com.google.gson.annotations.SerializedName

data class ProductOfferDto(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,

    @SerializedName("original_price")
    val originalPrice: Double? = null,

    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("store_name")
    val storeName: String? = null,

    @SerializedName("product_url")
    val productUrl: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,
)

fun ProductOfferDto.toProductOffer() = ProductOffer(
    id = id,
    store = storeName ?: "Tienda",
    productName = title,
    price = "$" + price.toString(),
    originalPrice = originalPrice?.let { "$$it" },
    shipping = "Consulta en tienda",
    delivery = "Disponibilidad por confirmar",
    rating = "—",
    sourceUrl = productUrl.orEmpty(),
    summary = description.orEmpty(),
    tags = if (originalPrice != null && originalPrice > price) {
        listOf("Oferta")
    } else {
        emptyList()
    },
    isBestValue = false,
)