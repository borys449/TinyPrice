package com.apps.iatinyfish.presentation.deals

import com.apps.iatinyfish.data.model.ProductOffer
import com.apps.iatinyfish.data.repository.DealsRepository
import com.apps.iatinyfish.data.repository.RemoteSearchException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeDealsRepository(
    private val result: List<ProductOffer> = emptyList(),
    private val failure: RemoteSearchException? = null,
) : DealsRepository {
    override val isPreviewData = true
    override suspend fun search(query: String): List<ProductOffer> {
        failure?.let { throw it }
        return result
    }
}

private fun sampleOffer() = ProductOffer(
    id = "1", store = "Tienda", productName = "Switch OLED", price = "\$300",
    shipping = "Gratis", delivery = "2 días", rating = "4.5",
    sourceUrl = "https://tienda.example", summary = "Resumen", tags = listOf("Oferta"),
)

@OptIn(ExperimentalCoroutinesApi::class)
class DealsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun validQueryPublishesResults() = runTest {
        val offer = sampleOffer()
        val vm = DealsViewModel(FakeDealsRepository(result = listOf(offer)))

        vm.onEvent(DealsEvent.QueryChanged("Switch OLED"))
        vm.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals(listOf(offer), vm.state.value.offers)
    }

    @Test fun blankQueryShowsMessageWithoutSearching() = runTest {
        val vm = DealsViewModel(FakeDealsRepository(result = listOf(sampleOffer())))

        vm.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()

        assertNotNull(vm.state.value.errorMessage)
        assertEquals(emptyList<ProductOffer>(), vm.state.value.offers)
    }

    @Test fun remoteErrorIsShown() = runTest {
        val vm = DealsViewModel(FakeDealsRepository(failure = RemoteSearchException("Sin conexión")))

        vm.onEvent(DealsEvent.QueryChanged("algo"))
        vm.onEvent(DealsEvent.SubmitSearch)
        advanceUntilIdle()

        assertEquals("Sin conexión", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }
}
