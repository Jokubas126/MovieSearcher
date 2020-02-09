package com.example.moviesearcher.viewmodel;

import android.app.Activity;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.moviesearcher.model.data.Movie;
import com.example.moviesearcher.model.data.Subcategory;
import com.example.moviesearcher.model.handlers.JsonHandler;
import com.example.moviesearcher.util.BundleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MovieGridViewModel extends ViewModel {

    private Activity activity;

    private MutableLiveData<List<Movie>> movies = new MutableLiveData<>();
    private MutableLiveData<Boolean> movieLoadError = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>();

    private int page;
    private boolean isListFull;
    private String listKey;
    private Subcategory subcategory;
    private String startYear, endYear;
    private String searchKey;

    public void initFetch(Activity activity, Bundle args){
        this.activity = activity;
        movieLoadError.setValue(false);
        loading.setValue(true);
        isListFull = false;
        if (args != null){
            listKey = args.getString(BundleUtil.KEY_MOVIE_LIST_TYPE);
            subcategory = args.getParcelable(BundleUtil.KEY_SUBCATEGORY);
            startYear = args.getString("start_year");
            endYear = args.getString("end_year");
            searchKey = args.getString(BundleUtil.KEY_SEARCH_QUERY);
        }
        if (listKey == null && subcategory == null && searchKey == null)
            listKey = BundleUtil.KEY_POPULAR;
        if (page == 0){
            page = 1;
            clearAll();
            getMovieList();
        } else {
            loading.setValue(false);
        }
    }

    public void refresh(){
        isListFull = false;
        movieLoadError.setValue(false);
        loading.setValue(true);
        page = 1;
        clearAll();
        getMovieList();
    }

    public void fetch(){
        if (!isListFull){
            movieLoadError.setValue(false);
            loading.setValue(true);
            page++;
            getMovieList();
        }
    }

    private void getMovieList(){
        new Thread(() -> new JsonHandler().getMovieList(listKey, subcategory, searchKey, startYear, endYear, page, list -> {
            if (list == null){
                activity.runOnUiThread(() -> {
                    isListFull = true;
                    if (Objects.requireNonNull(movies.getValue()).isEmpty())
                        movieLoadError.setValue(true);
                    loading.setValue(false);
                });
            } else {
                activity.runOnUiThread(() -> {
                    movies.setValue(list);
                    movieLoadError.setValue(false);
                    loading.setValue(false);
                });
            }
        })).start();
    }

    private void clearAll(){
        movies.setValue(new ArrayList<>());
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public LiveData<Boolean> getMovieLoadError() {
        return movieLoadError;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }
}
