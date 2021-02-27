package com.example.mmdb.navigation.actions

import android.os.Parcelable
import com.example.mmdb.navigation.Action
import kotlinx.android.parcel.Parcelize

@Parcelize
class RemoteMovieGridFragmentAction(
    val movieListType: MovieListType = MovieListType.Popular
) : WrappedFragmentAction

@Parcelize
open class MovieListType : Parcelable {

    @Parcelize
    object Popular : MovieListType()

    @Parcelize
    object TopRated : MovieListType()

    @Parcelize
    object NowPlaying : MovieListType()

    @Parcelize
    object Upcoming : MovieListType()

    @Parcelize
    data class Discover(
        val startYear: String? = null,
        val endYear: String? = null,
        val genreKeys: List<String?> = listOf(),
        val languageKeys: List<String?> = listOf()
    ) : MovieListType() {

        val discoverNameList =
            listOf(
                startYear?.let { "From: $startYear" },
                "To: $endYear"
            )
                .plus(genreKeys)
                .plus(languageKeys)
    }

    @Parcelize
    data class Search(val searchQuery: String? = null) : MovieListType()
}