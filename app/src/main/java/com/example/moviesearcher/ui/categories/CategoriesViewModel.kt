package com.example.moviesearcher.ui.categories

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.example.moviesearcher.model.data.Category
import com.example.moviesearcher.model.data.Genres
import com.example.moviesearcher.model.data.Subcategory
import com.example.moviesearcher.model.repositories.CategoryRepository
import com.example.moviesearcher.util.GENRE_CATEGORY
import com.example.moviesearcher.util.LANGUAGE_CATEGORY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.sort

class CategoriesViewModel : ViewModel() {

    private val _categories = MutableLiveData<MutableList<Category>>()
    private val _loading = MutableLiveData<Boolean>()

    val categories: LiveData<MutableList<Category>> = _categories
    val loading: LiveData<Boolean> = _loading

    fun fetch() {
        if (categories.value.isNullOrEmpty()) {
            _loading.value = true
            getLanguages()
            getGenres()
        }
    }

    private fun getLanguages() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = CategoryRepository().getLanguages()
            withContext(Dispatchers.Main) {
                val subcategories = prepareSubcategories(response.body()!!)
                var list = mutableListOf<Category>()
                if (!categories.value.isNullOrEmpty())
                    list = categories.value!!
                list.add(Category(LANGUAGE_CATEGORY, subcategories))
                _categories.value = list
                _loading.value = false
            }
        }
    }

    private fun getGenres() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = CategoryRepository().getGenres()
            withContext(Dispatchers.Main) {
                val subcategories = prepareSubcategories(genresToSubcategoryList(response.body()!!))
                var list = mutableListOf<Category>()
                if (!categories.value.isNullOrEmpty())
                    list = categories.value!!
                list.add(Category(GENRE_CATEGORY, subcategories))
                _categories.value = list
                _loading.value = false
            }
        }
    }

    private fun genresToSubcategoryList(genres: Genres): List<Subcategory> {
        val subcategoryList = mutableListOf<Subcategory>()
        for (genre in genres.genreList) {
            subcategoryList.add(Subcategory(genre.id.toString(), genre.name))
        }
        return subcategoryList
    }

    private fun prepareSubcategories(list: List<Subcategory>): List<Subcategory> {
        sort(list) { o1: Subcategory, o2: Subcategory -> o1.name.compareTo(o2.name) }
        (list as MutableList<Subcategory>).add(0, Subcategory("", "")) // to have an empty item at the beginning for deselection
        return list
    }

    // ------------ Navigation related -------------//

    private var languageSubcategory: Subcategory? = null
    private var genreSubcategory: Subcategory? = null

    fun onSubcategoryClicked(checked: Boolean, category: Category, subcategoryIndex: Int) {
        if (checked) {
            if ((category.items[subcategoryIndex] as Subcategory).name.isBlank()) {
                when (category.name) {
                    LANGUAGE_CATEGORY -> languageSubcategory = null
                    GENRE_CATEGORY -> genreSubcategory = null
                }
            } else {
                when (category.name) {
                    LANGUAGE_CATEGORY -> languageSubcategory =
                        category.items[subcategoryIndex] as Subcategory
                    GENRE_CATEGORY -> genreSubcategory =
                        category.items[subcategoryIndex] as Subcategory
                }
            }
        } else {
            when (category.name) {
                LANGUAGE_CATEGORY -> languageSubcategory = null
                GENRE_CATEGORY -> genreSubcategory = null
            }
        }
    }

    fun onConfirmSelectionClicked(view: View, startYear: String, endYear: String) {
        val action = when {
            genreSubcategory == null -> {
                CategoriesFragmentDirections.actionDiscoverGridFragment(
                    startYear,
                    endYear,
                    arrayOf(languageSubcategory?.name),
                    0,
                    languageSubcategory?.code
                )

            }
            languageSubcategory == null -> {
                CategoriesFragmentDirections.actionDiscoverGridFragment(
                    startYear,
                    endYear,
                    arrayOf(genreSubcategory?.name),
                    Integer.parseInt(genreSubcategory!!.code),
                    null
                )
            }
            else -> {
                CategoriesFragmentDirections.actionDiscoverGridFragment(
                    startYear,
                    endYear,
                    arrayOf(languageSubcategory?.name, genreSubcategory?.name),
                    Integer.parseInt(genreSubcategory!!.code),
                    languageSubcategory?.code
                )
            }
        }
        Navigation.findNavController(view).navigate(action)
    }
}