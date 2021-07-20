package com.example.mmdb.ui.movielists.pageselection

import android.view.View
import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import com.example.mmdb.BR
import com.example.mmdb.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.ItemBinding

class PageSelectionListViewModel(collectCoroutineScope: CoroutineScope) {

    val pageSelectionListVisibility: ObservableInt = ObservableInt(View.GONE)

    private val _currentPage: MutableStateFlow<Int> = MutableStateFlow(1)
    val currentPage: StateFlow<Int>
        get() = _currentPage

    private val totalPages: ObservableInt = ObservableInt(1)

    val itemsPage = ObservableArrayList<ItemPageViewModel>()

    val itemPageBinding: ItemBinding<ItemPageViewModel> =
        ItemBinding.of(BR.viewModel, R.layout.item_page)

    init {
        collectCoroutineScope.launch {
            currentPage.collect { page ->
                itemsPage.find { it.isCurrentPage.get() }?.isCurrentPage?.set(false)
                itemsPage.find { it.pageNumber == currentPage.value }?.isCurrentPage?.set(true)
            }

        }
        totalPages.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                itemsPage.removeAll { true }
                populatePageSelectionList()
            }
        })
    }

    private fun populatePageSelectionList() {
        IntRange(1, totalPages.get()).forEach {
            itemsPage.add(
                ItemPageViewModel(
                    pageNumber = it,
                    isCurrentPage = ObservableBoolean(it == currentPage.value),
                    onSelected = { pageNumber ->
                        _currentPage.value = pageNumber
                    }
                )
            )
        }
    }

    fun update(currentPage: Int, totalPages: Int) {
        if (this.currentPage.value != currentPage)
            this._currentPage.value = currentPage

        if (this.totalPages.get() != totalPages)
            this.totalPages.set(totalPages)

        pageSelectionListVisibility.set(if (totalPages > 1) View.VISIBLE else View.GONE)
    }
}
