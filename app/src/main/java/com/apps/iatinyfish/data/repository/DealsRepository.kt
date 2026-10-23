package com.apps.iatinyfish.data.repository

import com.apps.iatinyfish.data.model.ProductOffer

interface DealsRepository {
    val isPreviewData: Boolean
    suspend fun search(query: String): List<ProductOffer>
}

/** Datos ficticios para probar la interfaz sin red. */
object PreviewDealsRepository : DealsRepository {
    override val isPreviewData = true

    private val offers = listOf(
        ProductOffer(
            id = "pixel-store",
            store = "Pixel Store",
            productName = "Nintendo Switch OLED · Blanca",
            price = "\$329",
            originalPrice = "\$349",
            shipping = "Envío gratis",
            delivery = "Llega en 2–4 días",
            rating = "4.8",
            sourceUrl = "https://tienda.ejemplo.com/switch-oled",
            summary = "El precio más bajo de la búsqueda. Incluye consola, Joy-Con blancos y base OLED.",
            tags = listOf("Mejor precio", "Verificada hoy"),
            isBestValue = true,
        ),
        ProductOffer(
            id = "game-hub",
            store = "Game Hub",
            productName = "Nintendo Switch OLED · Neon",
            price = "\$339",
            shipping = "Envío gratis",
            delivery = "Disponible hoy",
            rating = "4.7",
            sourceUrl = "https://gamehub.ejemplo.com/switch-oled",
            summary = "Disponible para entrega rápida. Incluye una tarjeta de regalo.",
            tags = listOf("Entrega rápida", "Bonus incluido"),
        ),
        ProductOffer(
            id = "sound-lab",
            store = "Sound Lab",
            productName = "Auriculares Bluetooth con cancelación de ruido",
            price = "\$89",
            originalPrice = "\$120",
            shipping = "Envío \$4",
            delivery = "3–5 días",
            rating = "4.5",
            sourceUrl = "https://soundlab.ejemplo.com/auriculares",
            summary = "Hasta 30 horas de batería y estuche de carga.",
            tags = listOf("Oferta"),
        ),
    )

    override suspend fun search(query: String): List<ProductOffer> {
        if (query.isBlank()) return offers
        val terms = query.lowercase().split(" ").filter(String::isNotBlank)
        return offers.filter { offer ->
            val text = "${offer.store} ${offer.productName} ${offer.summary} ${offer.tags}".lowercase()
            terms.any(text::contains)
        }
    }
}
