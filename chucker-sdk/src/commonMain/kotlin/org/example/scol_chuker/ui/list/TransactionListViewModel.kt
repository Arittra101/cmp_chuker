package org.example.scol_chuker.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.scol_chuker.data.db.HttpTransaction
import org.example.scol_chuker.data.repository.TransactionRepository

internal class TransactionListViewModel(
    private val repository: TransactionRepository,
) : ViewModel() {

    val transactions: StateFlow<List<HttpTransaction>> = repository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}
