package com.apps.iatinyfish.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TinyPriceApi {

    @GET("rest/v1/product_offers")
    suspend fun getProductOffers(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("or") filter: String? = null,
        @Query("order") order: String = "created_at.desc",
    ): Response<List<ProductOfferDto>>
}
