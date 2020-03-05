package com.example.moviesearcher.model.services;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.moviesearcher.model.data.MovieOld;
import com.example.moviesearcher.model.data.Person;
import com.example.moviesearcher.model.data.Subcategory;
import com.example.moviesearcher.model.data.Video;
import com.example.moviesearcher.model.services.responses.GenresMapAsyncResponse;
import com.example.moviesearcher.model.services.responses.ImageListAsyncResponse;
import com.example.moviesearcher.model.services.responses.ObjectAsyncResponse;
import com.example.moviesearcher.model.services.responses.PersonListAsyncResponse;
import com.example.moviesearcher.model.services.responses.SubcategoryListAsyncResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.moviesearcher.util.MovieDbConfigKt.*;
import static com.example.moviesearcher.util.MovieDbUtilKt.*;

public class MovieDbApiService {

    private HashMap<Integer, String> genres = new HashMap<>();

    public HashMap<Integer, String> getGenres(final GenresMapAsyncResponse callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getMovieGenresUrl(), null,
                response -> {
                    Thread genreThread = new Thread(() -> {
                        try {
                            JSONArray array = response.getJSONArray(KEY_MOVIE_GENRES_ARRAY);
                            for (int i = 0; i < array.length(); i++) {
                                genres.put(
                                        array.getJSONObject(i).getInt(KEY_ID),
                                        array.getJSONObject(i).getString(KEY_NAME)
                                );
                            }
                        } catch (JSONException e) {
                            Log.d("JSONArrayRequest", "getGenresList: EXCEPTION OCCURRED");
                        }
                        if (callback != null) callback.processFinished(genres);
                    });
                    genreThread.setPriority(10);
                    genreThread.start();
                    try {
                        genreThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSONArrayRequest", "getGenresList: ERROR OCCURRED"));
        ApplicationRequestHandler.getInstance().addToRequestQueue(request);
        return genres;
    }

    public void getMovieDetails(int movieId, final ObjectAsyncResponse callback) {
        new Thread(() -> {
            MovieOld movieOld = new MovieOld();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                    getMovieDetailsUrl(movieId), null,
                    response -> new Thread(() -> {
                        try {
                            movieOld.setPosterImageUrl(getImageUrl(response.getString(KEY_POSTER_PATH)));
                            movieOld.setBackdropImageUrl(getImageUrl(response.getString(KEY_BACKDROP_PATH)));

                            movieOld.setId(movieId);
                            movieOld.setTitle(response.getString(KEY_MOVIE_TITLE));
                            movieOld.setReleaseDate(response.getString(KEY_RELEASE_DATE));

                            JSONArray countries = response.getJSONArray(KEY_PRODUCTION_COUNTRIES_ARRAY);
                            List<String> countryList = new ArrayList<>();
                            for (int i = 0; i < countries.length(); i++) {
                                countryList.add(countries.getJSONObject(i).getString(KEY_COUNTRY_ISO_CODE));
                            }
                            movieOld.setProductionCountries(countryList);

                            JSONArray genres = response.getJSONArray(KEY_MOVIE_GENRES_ARRAY);
                            List<String> genresList = new ArrayList<>();
                            for (int i = 0; i < genres.length(); i++) {
                                genresList.add(genres.getJSONObject(i).getString(KEY_NAME));
                            }
                            movieOld.setGenres(genresList);

                            movieOld.setRuntime(response.getInt(KEY_MOVIE_RUNTIME));
                            movieOld.setScore(response.getString(KEY_MOVIE_SCORE));
                            movieOld.setDescription(response.getString(KEY_MOVIE_DESCRIPTION));

                        } catch (JSONException e) {
                            Log.d("JSONArrayRequest", "getMovieDetails: EXCEPTION OCCURRED");
                        }
                        if (callback != null) callback.processFinished(movieOld);
                    }).start(), error -> Log.d("JSONArrayRequest", "getMovieDetails: ERROR OCCURRED"));
            ApplicationRequestHandler.getInstance().addToRequestQueue(request);
        }).start();
    }

    public void getTrailer(int movieId, final ObjectAsyncResponse callback) {
        new Thread(() -> {
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET, getMovieVideosUrl(movieId), null,
                    response -> new Thread(() -> {
                        try {
                            JSONArray array = response.getJSONArray(KEY_RESULT_ARRAY);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                if (object.getString(KEY_VIDEO_SITE).equals(KEY_YOUTUBE_SITE)
                                        && object.getString(KEY_VIDEO_TYPE).equals(KEY_TRAILER_TYPE)) {
                                    Video video = new Video();
                                    video.setKey(object.getString(KEY_VIDEO));
                                    video.setName(object.getString(KEY_NAME));
                                    if (callback != null) callback.processFinished(video);
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            Log.d("JSONObjectRequest", "getVideos: EXCEPTION OCCURRED");
                        }
                    }).start(), error -> Log.d("JSONObjectRequest", "getVideos: ERROR OCCURRED"));
            ApplicationRequestHandler.getInstance().addToRequestQueue(request);
        }).start();
    }

    public void getImages(int movieId, final ImageListAsyncResponse callback) {
        new Thread(() -> {
            List<String> backdropPaths = new ArrayList<>();
            List<String> posterPaths = new ArrayList<>();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getMovieImagesUrl(movieId), null,
                    response -> new Thread(() -> {
                        try {
                            JSONArray backdropArray = response.getJSONArray("backdrops");
                            for (int i = 0; i < backdropArray.length(); i++) {
                                backdropPaths.add(getImageUrl(backdropArray.getJSONObject(i).getString("file_path")));
                            }
                            JSONArray posterArray = response.getJSONArray("posters");
                            for (int i = 0; i < posterArray.length(); i++) {
                                if (i > 7)
                                    break;
                                posterPaths.add(getImageUrl(posterArray.getJSONObject(i).getString("file_path")));
                            }
                        } catch (JSONException e) {
                            Log.d("JSONObjectRequest", "getImages: EXCEPTION OCCURRED");
                        }
                        if (callback != null) callback.processFinished(backdropPaths, posterPaths);
                    }).start(), error -> Log.d("JSONObjectRequest", "getImages: ERROR OCCURRED"));
            ApplicationRequestHandler.getInstance().addToRequestQueue(request);
        }).start();
    }

    public void getPeople(int movieId, final PersonListAsyncResponse callback) {
        new Thread(() -> {
            List<Person> cast = new ArrayList<>();
            List<Person> crew = new ArrayList<>();
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getPeopleUrl(movieId), null,
                    response -> new Thread(() -> {
                        try {
                            JSONArray castArray = response.getJSONArray(KEY_CAST_ARRAY);
                            for (int i = 0; i < castArray.length(); i++) {

                                Person person = new Person();
                                JSONObject object = castArray.getJSONObject(i);

                                person.setName(object.getString(KEY_NAME));
                                person.setPosition(object.getString(KEY_CAST_POSITION));
                                if (!object.getString(KEY_PROFILE_IMAGE_PATH).equals("null"))
                                    person.setProfileImageUrl(getImageUrl(object.getString(KEY_PROFILE_IMAGE_PATH)));
                                cast.add(person);
                            }
                            JSONArray crewArray = response.getJSONArray(KEY_CREW_ARRAY);
                            for (int i = 0; i < crewArray.length(); i++) {

                                Person person = new Person();
                                JSONObject object = crewArray.getJSONObject(i);
                                person.setName(object.getString(KEY_NAME));
                                person.setPosition(object.getString(KEY_CREW_POSITION));
                                if (!object.getString(KEY_PROFILE_IMAGE_PATH).equals("null"))
                                    person.setProfileImageUrl(getImageUrl(object.getString(KEY_PROFILE_IMAGE_PATH)));
                                crew.add(person);
                            }
                        } catch (JSONException e) {
                            Log.d("JSONArrayRequest", "getPeople: EXCEPTION OCCURRED");
                        }
                        if (callback != null) callback.processFinished(cast, crew);
                    }).start()
                    , error -> Log.d("JSONArrayRequest", "getPeople: ERROR OCCURRED"));
            ApplicationRequestHandler.getInstance().addToRequestQueue(request);

        }).start();
    }

    public void getLanguages(final SubcategoryListAsyncResponse callback) {
        new Thread(() -> {
            List<Subcategory> subcategoryList = new ArrayList<>();

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getLanguagesUrl(), null,
                    response -> {
                        new Thread(() -> {
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject object = response.getJSONObject(i);
                                    subcategoryList.add(new Subcategory(object.getString(KEY_LANGUAGE_ISO_CODE), object.getString(KEY_ENGLISH_NAME)));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (callback != null) callback.processFinished(subcategoryList);
                        }).start();
                    }, error -> {

            });
            ApplicationRequestHandler.getInstance().addToRequestQueue(request);
        }).start();
    }
}
