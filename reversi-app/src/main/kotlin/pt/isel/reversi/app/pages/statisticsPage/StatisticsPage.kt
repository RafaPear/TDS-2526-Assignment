package pt.isel.reversi.app.pages.statisticsPage

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.reversi.app.ScaffoldView
import pt.isel.reversi.app.state.*
import pt.isel.reversi.app.utils.PreviousPage
import pt.isel.reversi.utils.TRACKER
import pt.isel.reversi.utils.TrackingStats
import java.time.format.DateTimeFormatter

/**
 * Section header composable for organizing statistics into logical groups.
 */
@Composable
private fun ReversiScope.StatsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReversiText(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider(
            color = getTheme().textColor.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        content()
    }
}

/**
 * Statistics page displaying tracking data from the application.
 * Features search, filtering, sorting, and detailed statistics display.
 *
 * @param viewModel The view model containing statistics data and actions.
 * @param modifier Optional modifier to adjust layout.
 * @param onLeave Callback invoked when navigating away from the page.
 */
@Composable
fun ReversiScope.StatisticsPage(
    viewModel: StatisticsPageViewModel,
    modifier: Modifier = Modifier,
    onLeave: () -> Unit
) {
    TRACKER.trackPageEnter(customName = "StatisticsPage", category = Page.STATISTICS)

    val state = viewModel.uiState.value
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    ScaffoldView(
        setError = { error -> viewModel.setError(error) },
        error = state.screenState.error,
        isLoading = state.screenState.isLoading,
        title = "Estatísticas de Tracking",
        previousPageContent = {
            PreviousPage { onLeave() }
        }
    ) { padding ->
        val scrollState = rememberScrollState(0)

        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            // Scrollbar for desktop
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = scrollState),
                style = ScrollbarStyle(
                    minimalHeight = 16.dp,
                    thickness = 8.dp,
                    shape = RoundedCornerShape(4.dp),
                    hoverDurationMillis = 300,
                    unhoverColor = getTheme().primaryColor.copy(alpha = 0.12f),
                    hoverColor = getTheme().primaryColor.copy(alpha = 0.24f)
                )
            )

            Column(
                modifier = modifier
                    .padding(vertical = 24.dp)
                    .widthIn(max = 900.dp)
                    .fillMaxWidth(0.95f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Summary section with cards
                SummarySection(
                    totalContexts = state.totalContexts,
                    totalEvents = state.totalEvents,
                    totalCategories = state.totalCategories,
                    onRefresh = { viewModel.loadStatistics() },
                    onClear = { viewModel.clearStatistics() }
                )

                // Search and filters section
                SearchAndFiltersSection(
                    searchQuery = state.searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    sortBy = state.sortBy,
                    onSortChange = { viewModel.setSortBy(it) },
                    showOnlyCategories = state.showOnlyCategories,
                    onToggleCategoryView = { viewModel.toggleCategoryView() }
                )

                // Display statistics based on view mode
                if (state.showOnlyCategories && state.categorizedStats.isNotEmpty()) {
                    CategoryView(
                        categorizedStats = state.categorizedStats,
                        dateFormatter = dateFormatter
                    )
                } else {
                    AllStatsView(
                        filteredStats = state.filteredStats,
                        dateFormatter = dateFormatter
                    )
                }
            }
        }
    }
}

/**
 * Summary section showing total statistics and action buttons.
 */
@Composable
private fun ReversiScope.SummarySection(
    totalContexts: Int,
    totalEvents: Long,
    totalCategories: Int,
    onRefresh: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats cards
        StatCard(
            title = "Contextos",
            value = totalContexts.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Eventos",
            value = totalEvents.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Categorias",
            value = totalCategories.toString(),
            modifier = Modifier.weight(1f)
        )
    }

    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Atualizar",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            ReversiText("Atualizar", fontSize = 14.sp)
        }

        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Limpar",
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            ReversiText("Limpar", fontSize = 14.sp)
        }
    }
}

/**
 * Individual stat card for summary display.
 */
@Composable
private fun ReversiScope.StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = getTheme().primaryColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReversiText(
                text = title,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
            ReversiText(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Search and filters section.
 */
@Composable
private fun ReversiScope.SearchAndFiltersSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: StatsSortBy,
    onSortChange: (StatsSortBy) -> Unit,
    showOnlyCategories: Boolean,
    onToggleCategoryView: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        ReversiTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = { ReversiText("Procurar contexto...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort dropdown
            var sortExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { sortExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    ReversiText("Ordenar: ${sortBy.displayName()}", fontSize = 14.sp)
                }

                ReversiDropDownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    StatsSortBy.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { ReversiText(sort.displayName()) },
                            onClick = {
                                onSortChange(sort)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }

            // View toggle button
            OutlinedButton(
                onClick = onToggleCategoryView,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (showOnlyCategories) Icons.AutoMirrored.Filled.ViewList else Icons.Default.Category,
                    contentDescription = "Alternar vista",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Category-based view of statistics.
 */
@Composable
private fun ReversiScope.CategoryView(
    categorizedStats: Map<Any, List<TrackingStats>>,
    dateFormatter: DateTimeFormatter
) {
    StatsSection(title = "Estatísticas por Categoria") {
        categorizedStats.entries.sortedBy { it.key.toString() }.forEach { (category, statsList) ->
            CategoryCard(
                categoryName = category.toString(),
                stats = statsList.sortedByDescending { it.eventCount.get() },
                dateFormatter = dateFormatter
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Card displaying stats for a category.
 */
@Composable
private fun ReversiScope.CategoryCard(
    categoryName: String,
    stats: List<TrackingStats>,
    dateFormatter: DateTimeFormatter
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = getTheme().backgroundColor.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReversiText(
                    text = categoryName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                ReversiText(
                    text = "${stats.size} contextos",
                    fontSize = 12.sp
                )
            }

            // Expandable content
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(
                    color = getTheme().textColor.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                Spacer(Modifier.height(12.dp))

                stats.forEach { stat ->
                    StatDetailCard(stat, dateFormatter)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * All stats view (list view).
 */
@Composable
private fun ReversiScope.AllStatsView(
    filteredStats: List<TrackingStats>,
    dateFormatter: DateTimeFormatter
) {
    StatsSection(title = "Todos os Contextos (${filteredStats.size})") {
        if (filteredStats.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getTheme().backgroundColor.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ReversiText(
                        text = "Nenhum resultado encontrado",
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            filteredStats.forEach { stat ->
                StatDetailCard(stat, dateFormatter)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * Detailed card for a single tracking stat.
 */
@Composable
private fun ReversiScope.StatDetailCard(
    stat: TrackingStats,
    dateFormatter: DateTimeFormatter
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = getTheme().backgroundColor.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, getTheme().primaryColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Context name header
            ReversiText(
                text = stat.context,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(
                color = getTheme().textColor.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    StatItem("Total de Eventos", stat.eventCount.get().toString())
                    StatItem("Entradas de Página", stat.pageEnters.get().toString())
                    StatItem("ViewModels", stat.viewModelCreations.get().toString())
                    StatItem("Classes", stat.classCreations.get().toString())
                }
                Column(modifier = Modifier.weight(1f)) {
                    StatItem("Recomposições", stat.recompositions.get().toString(), highlight = stat.recompositions.get() > 10)
                    StatItem("Chamadas", stat.functionCalls.get().toString())
                    StatItem("Início Efeito", stat.effectStarts.get().toString())
                    StatItem("Fim Efeito", stat.effectStops.get().toString())
                }
            }

            // Timestamps
            if (stat.firstOccurrence != null || stat.lastOccurrence != null) {
                HorizontalDivider(
                    color = getTheme().textColor.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                stat.firstOccurrence?.let {
                    ReversiText(
                        text = "Primeiro: ${it.format(dateFormatter)}",
                        fontSize = 11.sp
                    )
                }
                stat.lastOccurrence?.let {
                    ReversiText(
                        text = "Último: ${it.format(dateFormatter)}",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Individual stat item in the grid.
 */
@Composable
private fun ReversiScope.StatItem(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReversiText(
            text = "$label:",
            fontSize = 12.sp
        )
        ReversiText(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Extension to get display name for sort options.
 */
private fun StatsSortBy.displayName(): String = when (this) {
    StatsSortBy.TOTAL_EVENTS -> "Total de Eventos"
    StatsSortBy.CONTEXT_NAME -> "Nome do Contexto"
    StatsSortBy.RECOMPOSITIONS -> "Recomposições"
    StatsSortBy.FUNCTION_CALLS -> "Chamadas de Função"
}
