package org.example.scol_chuker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.example.scol_chuker.data.db.HttpTransaction
import org.example.scol_chuker.data.repository.TransactionRepository

internal class TransactionDetailViewModel(
    private val repository: TransactionRepository,
    private val id: Long,
) : ViewModel() {

    val transaction: StateFlow<HttpTransaction?> = flow {
        emit(repository.getById(id))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
