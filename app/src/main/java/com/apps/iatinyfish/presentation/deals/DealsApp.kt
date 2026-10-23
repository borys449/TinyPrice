package com.apps.iatinyfish.presentation.deals

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apps.iatinyfish.data.model.ProductOffer

private val Suggestions = listOf("Nintendo Switch OLED", "Auriculares Bluetooth", "Laptop gamer")

@Composable
fun DealsApp(state: DealsUiState, onEvent: (DealsEvent) -> Unit) {
    BackHandler(enabled = state.selectedOffer != null) {
        onEvent(DealsEvent.Back)
    }
    AnimatedContent(
        targetState = state.selectedOffer,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
        label = "screen",
    ) { offer ->
        if (offer == null) DealsSearchScreen(state, onEvent)
        else DealDetailScreen(offer, onBack = { onEvent(DealsEvent.Back) })
    }
}

// ───────────────────────── Búsqueda ─────────────────────────

@Composable
private fun DealsSearchScreen(state: DealsUiState, onEvent: (DealsEvent) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .padding(top = 24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FishMark()
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "TinyPrice",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            "Búsqueda web inteligente",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { onEvent(DealsEvent.QueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("¿Qué quieres comparar?") },
                    leadingIcon = {
                        Text(
                            "⌕",
                            fontSize = 26.sp,
                            modifier = Modifier.semantics { contentDescription = "Buscar productos" },
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { onEvent(DealsEvent.SubmitSearch) }) {
                            Text("Buscar", fontWeight = FontWeight.Bold)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onEvent(DealsEvent.SubmitSearch) }),
                    shape = RoundedCornerShape(18.dp),
                )
            }
        },
    ) { padding ->
        if (state.hasSearched) {
            SearchResults(state, padding.calculateTopPadding(), onEvent)
        } else {
            DiscoveryHome(padding.calculateTopPadding()) { onEvent(DealsEvent.UseSuggestion(it)) }
        }
    }
}

@Composable
private fun FishMark() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text("◒", color = MaterialTheme.colorScheme.onPrimary, fontSize = 22.sp)
    }
}

@Composable
private fun DiscoveryHome(topPadding: Dp, onSuggestion: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = topPadding + 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Encuentra la mejor compra.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            "Compara fuentes sin abrir diez pestañas.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text("Prueba con:", fontWeight = FontWeight.Bold)
        Suggestions.forEach { suggestion ->
            OutlinedButton(
                onClick = { onSuggestion(suggestion) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) { Text(suggestion) }
        }
    }
}

@Composable
private fun SearchResults(
    state: DealsUiState,
    topPadding: Dp,
    onEvent: (DealsEvent) -> Unit,
) {
    when {
        state.isLoading -> LoadingState(topPadding)
        state.offers.isEmpty() -> EmptyState(
            message = state.errorMessage ?: "No encontramos ofertas para “${state.query}”",
            topPadding = topPadding,
            onRetry = { onEvent(DealsEvent.SubmitSearch) },
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp, topPadding + 6.dp, 20.dp, 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Resultados para “${state.query}”",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        if (state.isPreviewData) "Ofertas de demostración (no son reales)"
                        else "Resultados encontrados en la web",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.offers, key = ProductOffer::id) { offer ->
                OfferCard(offer) { onEvent(DealsEvent.OpenOffer(offer)) }
            }
        }
    }
}

@Composable
private fun LoadingState(topPadding: Dp) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = topPadding)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Buscando ofertas…", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState(message: String, topPadding: Dp, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 28.dp, end = 28.dp, top = topPadding)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Intentar de nuevo") }
    }
}

@Composable
private fun OfferCard(offer: ProductOffer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(
                        offer.store,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(offer.productName, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    offer.price,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(offer.shipping, style = MaterialTheme.typography.bodySmall)
                Text("★ ${offer.rating}", fontWeight = FontWeight.Bold)
                Text(offer.delivery, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ───────────────────────── Detalle ─────────────────────────

@Composable
private fun DealDetailScreen(offer: ProductOffer, onBack: () -> Unit) {
    val context = LocalContext.current
    val canOpen = offer.sourceUrl.startsWith("http://") || offer.sourceUrl.startsWith("https://")

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TextButton(onClick = onBack) { Text("← Volver a resultados") }
            Text(
                offer.store,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                offer.productName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    offer.price,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                )
                offer.originalPrice?.let {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "antes $it",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(offer.summary, style = MaterialTheme.typography.bodyLarge)
            HorizontalDivider()
            DetailRow("Envío", offer.shipping)
            DetailRow("Entrega", offer.delivery)
            DetailRow("Valoración", "★ ${offer.rating}")
            if (offer.tags.isNotEmpty()) DetailRow("Etiquetas", offer.tags.joinToString(", "))
            DetailRow("Fuente", offer.sourceUrl.ifBlank { "No disponible" })
            Spacer(Modifier.height(8.dp))
            if (canOpen) {
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(offer.sourceUrl)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) { Text("Abrir fuente web") }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Text(value, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f, fill = false))
    }
}
