package org.getscol.scol_chuker.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.getscol.scol_chuker.data.db.TransactionSummary
import org.getscol.scol_chuker.data.repository.TransactionRepository

/** `statusCode` 0 is the inspector's "no response" marker, so [FAILED] is simply the 0..0 range. */
internal enum class StatusFilter(val label: String, val min: Int, val max: Int) {
    ALL("all", 0, 999),
    SUCCESS("2xx", 200, 299),
    REDIRECT("3xx", 300, 399),
    CLIENT_ERROR("4xx", 400, 499),
    SERVER_ERROR("5xx", 500, 599),
    FAILED("fail", 0, 0),
}

internal class TransactionListViewModel(
    private val repository: TransactionRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(StatusFilter.ALL)
    val filter: StateFlow<StatusFilter> = _filter.asStateFlow()

    /** `null` until the first query comes back — otherwise the UI claims "nothing captured" while
     *  the database is still opening, which reads as a bug to whoever is testing. */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionSummary>?> =
        combine(_query.debounce(150), _filter) { query, filter -> query to filter }
            .flatMapLatest { (query, filter) -> repository.search(query, filter.min, filter.max) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val totalCount: StateFlow<Int> = repository.observeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val errorCount: StateFlow<Int> = repository.observeErrorCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onFilterChange(value: StatusFilter) {
        _filter.value = value
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}
