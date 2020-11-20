package ru.mail.z_team.icon_fragments.profile;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.mail.z_team.network.ApiRepository;
import ru.mail.z_team.network.UserApi;

public class ProfileRepository {

    private static final String LOG_TAG = "ProfileRepository";
    private static final int PROBLEM_WITH_AUTH_CODE = 401;

    private final Context context;
    private final MutableLiveData<User> UserData = new MutableLiveData<>();

    private final UserApi userApi;

    public ProfileRepository(Context context) {
        this.context = context;
        userApi = ApiRepository.from(context).getUserApi();
    }

    public LiveData<User> getUserInfoById(final String id) {
        return UserData;
    }

    public void update(final String id) {
        log(id);
        userApi.getUserById(id).enqueue(new Callback<UserApi.User>() {
            @Override
            public void onResponse(Call<UserApi.User> call, Response<UserApi.User> response) {
                if (response.code() == PROBLEM_WITH_AUTH_CODE) {
                    errorLog("Problem with Auth", null);
                    return;
                }
                if (response.body() == null) {
                    errorLog("File not found", null);
                    return;
                }
                if (response.isSuccessful()) {
                    UserData.postValue(transformToUser(response.body()));
                }
            }

            @Override
            public void onFailure(Call<UserApi.User> call, Throwable t) {
                errorLog("Failed to load", t);
            }
        });
    }

    public void changeUserInformation(final String id, User newInformation) {
        userApi.addUser(id, newInformation.getUserApiUser()).enqueue(new Callback<UserApi.User>() {
            @Override
            public void onResponse(Call<UserApi.User> call, Response<UserApi.User> response) {

            }

            @Override
            public void onFailure(Call<UserApi.User> call, Throwable t) {

            }
        });
    }

    private void errorLog(final String message, Throwable t) {
        Log.e(LOG_TAG, message, t);
    }

    private void log(final String message) {
        Log.d(LOG_TAG, message);
    }

    private User transformToUser(UserApi.User user) {
        return new User(
                user.name,
                user.age,
                user.id
        );
    }
}
