package ru.mail.z_team.icon_fragments.news;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;
import ru.mail.z_team.icon_fragments.DatabaseCallback;
import ru.mail.z_team.icon_fragments.walks.Walk;
import ru.mail.z_team.network.ApiRepository;
import ru.mail.z_team.network.UserApi;

public class NewsRepository {

    private static final String LOG_TAG = "NewsRepository";

    private final UserApi userApi;

    private final MutableLiveData<ArrayList<Walk>> currentUserNews = new MutableLiveData<>();

    SimpleDateFormat sdf =
            new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");

    public NewsRepository(Context context) {
        userApi = ApiRepository.from(context).getUserApi();
    }

    public LiveData<ArrayList<Walk>> getNews() {
        log("get news " + currentUserNews.toString());
        return currentUserNews;
    }

    public void updateCurrentUserNews() {
        log("updateNews");
        String curId = FirebaseAuth.getInstance().getUid();
        userApi.getUserFriendsIds(curId).enqueue(new DatabaseCallback<ArrayList<String>>(LOG_TAG) {
            @Override
            public void onNullResponse(Response<ArrayList<String>> response) {
                currentUserNews.postValue(new ArrayList<>());
                log(curId + " doesn't have friends");
            }

            @Override
            public void onSuccessResponse(Response<ArrayList<String>> response) {
                log(curId + " have friends " + response.body().size());
                compileNewsAndPostInCurrentNews(response.body());
            }
        });
    }

    private void compileNewsAndPostInCurrentNews(ArrayList<String> ids) {
        ArrayList<Walk> news = new ArrayList<>();

        log("Compile news");
        for (String id : ids) {
            log("Compile news... " + ids.indexOf(id));
            userApi.getUserWalksById(id).enqueue(new DatabaseCallback<ArrayList<UserApi.Walk>>(LOG_TAG) {
                @Override
                public void onNullResponse(Response<ArrayList<UserApi.Walk>> response) {
                    log(id + " doesn't have walks");
                }

                @Override
                public void onSuccessResponse(Response<ArrayList<UserApi.Walk>> response) {
                    log(id + " have walks");
                    news.addAll(transformToWalkAll(response.body()));
                    Collections.sort(news);
                    currentUserNews.postValue(news);
                }
            });
        }
    }

    private ArrayList<Walk> transformToWalkAll(ArrayList<UserApi.Walk> walks) {
        ArrayList<Walk> result = new ArrayList<>();
        for (UserApi.Walk walk : walks) {
            result.add(transformToWalk(walk));
        }
        return result;
    }

    private Walk transformToWalk(UserApi.Walk walk) {
        Walk transformed = new Walk();
        transformed.setTitle(walk.title);
        transformed.setAuthor(walk.author);
        try {
            transformed.setDate(sdf.parse(walk.date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return transformed;
    }

    private void log(final String message) {
        Log.d(LOG_TAG, message);
    }
}
