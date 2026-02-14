package com.menulens.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.menulens.app.data.EntitlementState
import com.menulens.app.data.EntitlementStore
import com.menulens.app.model.MenuItem
import com.menulens.app.model.MenuPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultsUiState(
    val items: List<MenuItem> = emptyList(),
    val creditsRemainingToday: Int = EntitlementStore.DEFAULT_DAILY_CREDITS,
    val isPro: Boolean = false,
    val unlockedItemIds: Set<String> = emptySet()
) {
    fun itemById(itemId: String): MenuItem? = items.firstOrNull { it.itemId == itemId }
    fun isUnlocked(itemId: String): Boolean = isPro || unlockedItemIds.contains(itemId)
}

sealed interface ResultsNavEvent {
    data class OpenDetail(val itemId: String) : ResultsNavEvent
    data class OpenShowToStaff(val itemId: String) : ResultsNavEvent
    data object OpenPaywall : ResultsNavEvent
}

class ResultsViewModel(application: Application) : AndroidViewModel(application) {
    private val entitlementStore = EntitlementStore(application.applicationContext)

    private val unlockedItemIds = MutableStateFlow<Set<String>>(emptySet())
    private val staticItems = sampleMenuItems()

    private val entitlementState = entitlementStore.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntitlementState(3, false))

    val uiState: StateFlow<ResultsUiState> = combine(
        entitlementState,
        unlockedItemIds
    ) { entitlement, unlocked ->
        ResultsUiState(
            items = staticItems,
            creditsRemainingToday = entitlement.creditsRemainingToday,
            isPro = entitlement.isPro,
            unlockedItemIds = unlocked
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultsUiState(items = staticItems)
    )

    private val _navEvents = MutableSharedFlow<ResultsNavEvent>(extraBufferCapacity = 8)
    val navEvents = _navEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            entitlementStore.refreshCreditsIfNeeded()
        }
    }

    fun startNewScanSession() {
        viewModelScope.launch {
            entitlementStore.refreshCreditsIfNeeded()
            unlockedItemIds.value = emptySet()
        }
    }

    fun onResultItemSelected(itemId: String) {
        viewModelScope.launch {
            handleRevealOrNavigate(itemId)
        }
    }

    fun onRevealFromDetail(itemId: String) {
        viewModelScope.launch {
            handleRevealOrNavigate(itemId)
        }
    }

    fun onShowToStaff(itemId: String) {
        _navEvents.tryEmit(ResultsNavEvent.OpenShowToStaff(itemId))
    }

    fun setProEnabled(enabled: Boolean) {
        viewModelScope.launch {
            entitlementStore.setProEnabled(enabled)
        }
    }

    fun restorePurchasesStub() {
        viewModelScope.launch {
            entitlementStore.resetProForRestoreStub()
        }
    }

    private suspend fun handleRevealOrNavigate(itemId: String) {
        val current = uiState.value
        when (
            RevealPolicy.decide(
                isUnlocked = current.unlockedItemIds.contains(itemId),
                isPro = current.isPro,
                creditsRemainingToday = current.creditsRemainingToday
            )
        ) {
            RevealDecision.OPEN_ALREADY_UNLOCKED -> {
                _navEvents.emit(ResultsNavEvent.OpenDetail(itemId))
            }
            RevealDecision.OPEN_WITH_PRO -> {
                unlockedItemIds.update { it + itemId }
                _navEvents.emit(ResultsNavEvent.OpenDetail(itemId))
            }
            RevealDecision.CONSUME_CREDIT_AND_OPEN -> {
                val consumed = entitlementStore.tryConsumeCredit()
                if (consumed) {
                    unlockedItemIds.update { it + itemId }
                    _navEvents.emit(ResultsNavEvent.OpenDetail(itemId))
                } else {
                    _navEvents.emit(ResultsNavEvent.OpenPaywall)
                }
            }
            RevealDecision.OPEN_PAYWALL -> {
                _navEvents.emit(ResultsNavEvent.OpenPaywall)
            }
        }
    }

    private fun sampleMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                itemId = "item-1",
                jpText = "醤油ラーメン",
                priceText = "900円",
                preview = MenuPreview(
                    enTitle = "Shoyu Ramen",
                    enDescription = "Typically soy sauce-based ramen with sliced pork and green onion.",
                    tags = listOf("pork_possible"),
                    images = listOf("https://example.com/ramen.jpg")
                )
            ),
            MenuItem(
                itemId = "item-2",
                jpText = "親子丼",
                priceText = "850円",
                preview = MenuPreview(
                    enTitle = "Oyako-don",
                    enDescription = "Usually a rice bowl topped with chicken and egg simmered in sweet-savory broth.",
                    tags = listOf("egg", "chicken"),
                    images = listOf("https://example.com/oyakodon.jpg")
                )
            ),
            MenuItem(
                itemId = "item-3",
                jpText = "焼き魚定食",
                priceText = "1100円",
                preview = MenuPreview(
                    enTitle = "Grilled Fish Set",
                    enDescription = "Often includes grilled fish, rice, miso soup, and small side dishes.",
                    tags = listOf("fish_possible"),
                    images = listOf("https://example.com/fish-set.jpg")
                )
            )
        )
    }
}

