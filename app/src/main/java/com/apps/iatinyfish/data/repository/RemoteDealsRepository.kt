package com.apps.iatinyfish.data.repository

import com.apps.iatinyfish.BuildConfig
import com.apps.iatinyfish.data.model.ProductOffer
import com.apps.iatinyfish.data.remote.TinyPriceApi
import com.apps.iatinyfish.data.remote.TinyPriceApiFactory
import com.apps.iatinyfish.data.remote.toProductOffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteDealsRepository(
    private val api: TinyPriceApi = TinyPriceApiFactory.create(),
) : DealsRepository {

    override val isPreviewData = false

    override suspend fun search(query: String): List<ProductOffer> =
        withContext(Dispatchers.IO) {
            val key = BuildConfig.SUPABASE_ANON_KEY

            require(key.isNotBlank()) {
                "Configura la clave pública de Supabase."
            }

            val term = query.trim()

            // Sin búsqueda, devuelve las últimas ofertas.
            val filter = term.takeIf { it.isNotBlank() }
                ?.let {
                    val safe = it
                        .replace(",", " ")
                        .replace("(", " ")
                        .replace(")", " ")
                        .replace("*", " ")
                        .replace("%", " ")
                        .replace("_", " ")
                        .replace(".", " ")
                        .replace("\"", " ")
                        .replace("'", " ")
                        .trim()

                    if (safe.isBlank()) null
                    else "(title.ilike.*$safe*," +
                            "description.ilike.*$safe*," +
                            "store_name.ilike.*$safe*)"
                }

            val response = api.getProductOffers(
                apiKey = key,
                authorization = "Bearer $key",
                filter = filter,
            )

            if (!response.isSuccessful) {
                throw RemoteSearchException(
                    "Error de Supabase: HTTP ${response.code()}. " +
                            "Verifica la URL, la clave pública y las políticas RLS."
                )
            }

            response.body().orEmpty().map {
                it.toProductOffer()
            }
        }
}

class RemoteSearchException(message: String) : Exception(message)

object DealsRepositoryProvider {
    fun create(): DealsRepository =
        if (BuildConfig.USE_REMOTE_DEALS) {
            RemoteDealsRepository()
        } else {
            PreviewDealsRepository
        }
}