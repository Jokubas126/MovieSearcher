package com.example.mmdb.ui.movielists.rest

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.example.mmdb.BR
import com.example.mmdb.R
import com.example.mmdb.ui.movielists.ItemMovieConfig
import com.example.mmdb.ui.movielists.ItemMovieViewModel
import com.jokubas.mmdb.model.remote.repositories.RemoteMovieRepository
import com.jokubas.mmdb.model.room.repositories.GenresRepository
import com.jokubas.mmdb.model.room.repositories.WatchlistRepository
import com.example.mmdb.ui.movielists.toItemMovieViewModel
import com.jokubas.mmdb.model.data.entities.Movie
import com.jokubas.mmdb.model.data.entities.MovieResults
import com.jokubas.mmdb.model.data.entities.WatchlistMovie
import com.jokubas.mmdb.util.isNetworkAvailable
import com.jokubas.mmdb.util.networkUnavailableNotification
import com.jokubas.mmdb.util.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.ItemBinding

class RestMovieGridViewModel(
    application: Application,
    arguments: Bundle?,
    private val onMovieClicked: (movieId: Int) -> Unit
) : AndroidViewModel(application) {

    private val progressManager = RemoteListProgressManager()

    val error: LiveData<Boolean>
        get() = progressManager.error
    val loading: LiveData<Boolean>
        get() = progressManager.loading

    private val args = arguments?.let { RestMovieGridFragmentArgs.fromBundle(arguments) }

    // repositories
    private val remoteMovieRepository = RemoteMovieRepository()
    private val watchlistRepository = WatchlistRepository(application)
    private val genresRepository = GenresRepository(application)

    private var watchlistMovieIdList = watchlistRepository.getAllMovieIds().asLiveData().apply {
        observeForever {
            refresh()
        }
    }

    val pageSelectionListViewModel = ObservableField<PageSelectionListViewModel>()

    val itemsMovie = ObservableArrayList<ItemMovieViewModel>()
    val itemMoviesBinding: ItemBinding<ItemMovieViewModel> =
        ItemBinding.of(BR.viewModel, R.layout.item_movie)

    init {
        progressManager.loading()
        progressManager.checkPages()
        getResponse(progressManager.currentPage)
    }

    fun refresh() {
        progressManager.refresh()
        getResponse(progressManager.currentPage)
    }

    //TODO replace this with fetchData or smth like that
    fun addData() {
        if (!progressManager.isListFull) {
            progressManager.addingData()
            progressManager.checkPages()
            getResponse(progressManager.currentPage)
        }
    }

    private fun getResponse(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isNetworkAvailable(getApplication())) {
                args?.apply {
                    remoteMovieRepository.getMovieResults(
                        page,
                        movieGridType,
                        keyCategory,
                        startYear,
                        endYear,
                        genreId,
                        languageKey,
                        searchQuery
                    )?.let { configureResults(it) }
                } ?: progressManager.error()
            } else {
                progressManager.error()
                networkUnavailableNotification(getApplication())
            }
        }
    }

    private fun configureResults(results: MovieResults) {
        results.apply {

            progressManager.checkIfListFull(totalPages)

            for (movie in movieList)
                movie.formatGenresString(genresRepository.getGenresByIdList(movie.genreIds))

            movieList.forEach { movie ->
                movie.isInWatchlist = watchlistMovieIdList.value?.contains(movie.remoteId) == true
            }
        }

        insertMovieListToData(results)
    }

    private fun insertMovieListToData(results: MovieResults) {
        when (results.movieList.isNotEmpty()) {
            true -> {
                viewModelScope.launch {
                    pageSelectionListViewModel.set(
                        PageSelectionListViewModel(results.totalPages, results.page)
                    )

                    results.movieList.forEach { movie ->
                        itemsMovie.add(
                            movie.toItemMovieViewModel(
                                getItemMovieConfig(movie, results.page, itemsMovie.size)
                            )
                        )
                    }
                    progressManager.success()
                }
            }
            else -> progressManager.error()
        }
    }

    private fun getItemMovieConfig(movie: Movie, page: Int, position: Int) =
        ItemMovieConfig(
            position = position,
            page = page,
            onItemSelected = {
                onMovieClicked(movie.remoteId)
            },
            onCustomListSelected = {
                Toast.makeText(getApplication(), "Playlist clicked", Toast.LENGTH_SHORT).show()
                //onPlaylistAddCLicked(movie)
            },
            onWatchlistSelected = {
                updateWatchlist(movie)
            }
        )

//------------------------ Watchlist ----------------------------//

    private fun updateWatchlist(movie: Movie) {
        if (movie.isInWatchlist) {
            watchlistRepository.deleteWatchlistMovie(movie.remoteId)
            showToast(
                getApplication(),
                getApplication<Application>().getString(R.string.deleted_from_watchlist),
                Toast.LENGTH_SHORT
            )
        } else {
            watchlistRepository.insertOrUpdateMovie(WatchlistMovie(movie.remoteId))
            showToast(
                getApplication(),
                getApplication<Application>().getString(R.string.added_to_watchlist),
                Toast.LENGTH_SHORT
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        watchlistMovieIdList.removeObserver {}
    }
/*
//------------------ Custom lists --------------------------//

    private fun onPlaylistAddCLicked(movie: Movie) {
        AddToListsTaskManager(
            getApplication(),
            AddToListsPopupWindow(
                View.inflate(getApplication(), R.layout.popup_window_personal_lists_to_add, null),
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                movie
            )
        )
    }*/
}