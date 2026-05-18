package org.example.scol_chuker.ui.nav

import kotlinx.serialization.Serializable

@Serializable
internal object TransactionList

@Serializable
internal data class TransactionDetail(val id: Long)
