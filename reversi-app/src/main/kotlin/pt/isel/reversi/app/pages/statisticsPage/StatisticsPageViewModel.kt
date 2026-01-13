package pt.isel.reversi.app.pages.statisticsPage

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import pt.isel.reversi.app.state.ScreenState
import pt.isel.reversi.app.state.UiState
import pt.isel.reversi.app.state.ViewModel
import pt.isel.reversi.app.state.setError
import pt.isel.reversi.core.exceptions.ReversiException
import pt.isel.reversi.utils.TRACKER
import pt.isel.reversi.utils.TrackingStats

enum class StatsSortBy {
    TOTAL_EVENTS,
    CONTEXT_NAME,
    RECOMPOSITIONS,
    FUNCTION_CALLS
}

/**
 * UI state for the statistics page, including filters, counts, and screen state.
 *
 * @property categorizedStats Statistics grouped by category.
 * @property allStats All tracking statistics indexed by name.
 * @property filteredStats Currently filtered statistics based on search and sort criteria.
 * @property searchQuery The current search query for filtering statistics.
 * @property sortBy The current sort order for statistics.
 * @property showOnlyCategories Whether to show only category-level statistics.
 * @property totalContexts Total number of unique contexts in the data.
 * @property totalEvents Total number of tracked events.
 * @property totalCategories Total number of unique categories in the data.
 * @property screenState The screen state containing error and loading information.
 */
data class StatisticsUiState(
    val categorizedStats: Map<Any, List<TrackingStats>> = emptyMap(),
    val allStats: Map<String, TrackingStats> = emptyMap(),
    val filteredStats: List<TrackingStats> = emptyList(),
    val searchQuery: String = "",
    val sortBy: StatsSortBy = StatsSortBy.TOTAL_EVENTS,
    val showOnlyCategories: Boolean = false,
    val totalContexts: Int = 0,
    val totalEvents: Long = 0,
    val totalCategories: Int = 0,
    override val screenState: ScreenState = ScreenState()
) : UiState() {
    /**
     * Creates a copy of this UI state with the given screen state.
     * @param newScreenState The new screen state to apply.
     * @return A new StatisticsUiState with the updated screen state.
     */
    override fun updateScreenState(newScreenState: ScreenState) =
        copy(screenState = newScreenState)
}

/**
 * ViewModel for statistics page. Loads tracking data, exposes filters and sorted results.
 * Handles clearing and reloading of tracking stats.
 *
 * @param scope Coroutine scope for background operations (currently unused but reserved).
 * @param globalError Optional global error to seed initial screen state.
 */
class StatisticsPageViewModel(
    scope: CoroutineScope,
    globalError: ReversiException? = null
) : ViewModel {
    private val _uiState = mutableStateOf(
        StatisticsUiState(
            screenState = ScreenState(error = globalError)
        )
    )
    override val uiState: State<StatisticsUiState> = _uiState

    init {
        loadStatistics()
    }

    override fun setError(error: Exception?) =
        _uiState.setError(error)

    fun loadStatistics() {
        val categorizedStats = TRACKER.getCategorizedStats()
        val allStats = TRACKER.getAllStats()
        val totalEvents = TRACKER.getTotalEventsTracked()

        _uiState.value = _uiState.value.copy(
            categorizedStats = categorizedStats,
            allStats = allStats,
            totalContexts = allStats.size,
            totalEvents = totalEvents,
            totalCategories = categorizedStats.size
        )

        applyFilters()
    }

    fun clearStatistics() {
        TRACKER.clear()
        loadStatistics()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun setSortBy(sortBy: StatsSortBy) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        applyFilters()
    }

    fun toggleCategoryView() {
        _uiState.value = _uiState.value.copy(showOnlyCategories = !_uiState.value.showOnlyCategories)
    }

    private fun applyFilters() {
        val query = _uiState.value.searchQuery.lowercase()
        val allStats = _uiState.value.allStats.values

        val filtered = if (query.isEmpty()) {
            allStats.toList()
        } else {
            allStats.filter { it.context.lowercase().contains(query) }
        }

        val sorted = when (_uiState.value.sortBy) {
            StatsSortBy.TOTAL_EVENTS -> filtered.sortedByDescending { it.eventCount.get() }
            StatsSortBy.CONTEXT_NAME -> filtered.sortedBy { it.context }
            StatsSortBy.RECOMPOSITIONS -> filtered.sortedByDescending { it.recompositions.get() }
            StatsSortBy.FUNCTION_CALLS -> filtered.sortedByDescending { it.functionCalls.get() }
        }

        _uiState.value = _uiState.value.copy(filteredStats = sorted)
    }
}
