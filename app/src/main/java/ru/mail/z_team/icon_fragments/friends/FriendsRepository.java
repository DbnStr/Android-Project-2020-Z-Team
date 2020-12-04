package ru.mail.z_team.icon_fragments.friends;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import retrofit2.Response;
import ru.mail.z_team.icon_fragments.DatabaseCallback;
import ru.mail.z_team.network.ApiRepository;
import ru.mail.z_team.network.UserApi;
import ru.mail.z_team.user.Friend;
import ru.mail.z_team.user.User;

public class FriendsRepository {

    private static final String LOG_TAG = "FriendsRepository";

    private final UserApi userApi;

    private final MutableLiveData<User> currentUserData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> userExistence = new MutableLiveData<>();

    private int count;

    public FriendsRepository(Context context) {
        userApi = ApiRepository.from(context).getUserApi();
    }

    public LiveData<User> getCurrentUser() {
        return currentUserData;
    }

    public void updateCurrentUser() {
        String currentUserId = FirebaseAuth.getInstance().getUid();

        log("update user - " + currentUserId);

        userApi.getUserById(currentUserId).enqueue(new DatabaseCallback<UserApi.User>(LOG_TAG) {
            @Override
            public void onNullResponse(Response<UserApi.User> response) {
                errorLog("Fail with update", null);
            }

            @Override
            public void onSuccessResponse(Response<UserApi.User> response) {
                currentUserData.postValue(transformToUser(response.body()));
            }
        });
    }

    private User transformToUser(UserApi.User user) {
        String name = user.name;
        if (name == null) {
            name = "Anonymous";
        }
        ArrayList<Friend> userFriends = new ArrayList<>();
        if (user.friends != null) {
            for (UserApi.Friend friend : user.friends) {
                userFriends.add(transformToFriend(friend));
            }
        }
        return new User(
                name,
                user.age,
                user.id,
                userFriends
        );
    }

    private Friend transformToFriend(UserApi.Friend friend) {
        return new Friend(friend.name, friend.id);
    }

    public void checkUserExistence(String id) {
        log("checkUserExistence");
        userApi.getUserById(id).enqueue(new DatabaseCallback<UserApi.User>(LOG_TAG) {
            @Override
            public void onNullResponse(Response<UserApi.User> response) {
                Log.d(LOG_TAG, "posted false");
                userExistence.postValue(false);
            }

            @Override
            public void onSuccessResponse(Response<UserApi.User> response) {
                userExistence.postValue(true);
            }
        });
    }

    public LiveData<Boolean> userExists() {
        log("userExists");
        return userExistence;
    }

    public void addFriendToCurrentUser(String id, int num) {
        log("addFriend");
        String curUserId = FirebaseAuth.getInstance().getUid();

        userApi.getUserById(id).enqueue(new DatabaseCallback<UserApi.User>(LOG_TAG) {
            @Override
            public void onNullResponse(Response<UserApi.User> response) {
                errorLog("Failed to get " + id + " user", null);
            }

            @Override
            public void onSuccessResponse(Response<UserApi.User> response) {
                UserApi.Friend friend = transformToUserApiFriend(response.body());
                if (response.body().friends == null) {
                    count = 0;
                } else {
                    count = response.body().friends.size();
                }

                addFriendToUser(curUserId, num, friend);

                addFriendIdToFriendsIdsList(curUserId, num, friend.id);
            }
        });
        userApi.getUserById(curUserId).enqueue(new DatabaseCallback<UserApi.User>(LOG_TAG) {
            @Override
            public void onNullResponse(Response<UserApi.User> response) {
                errorLog("Failed to get " + curUserId + " user", null);
            }

            @Override
            public void onSuccessResponse(Response<UserApi.User> response) {
                UserApi.Friend friend = transformToUserApiFriend(response.body());

                addFriendToUser(id, count, friend);

                addFriendIdToFriendsIdsList(id, count, friend.id);
            }
        });
    }

    private UserApi.Friend transformToUserApiFriend(UserApi.User user) {
        UserApi.Friend result = new UserApi.Friend();
        result.id = user.id;
        result.name = user.name;
        return result;
    }

    private void addFriendIdToFriendsIdsList(final String user, final int number, final String friendId) {
        userApi.addFriendId(user, number, friendId).enqueue(new DatabaseCallback<String>(LOG_TAG) {
            @Override
            public void onNullResponse(Response<String> response) {
                log("failed to add friend id");
            }

            @Override
            public void onSuccessResponse(Response<String> response) {
                log("successfully add friend id");
            }
        });
    }

    private void addFriendToUser(final String userId, final int number, final UserApi.Friend friend) {
        userApi.addFriend(userId, number, friend).enqueue(new DatabaseCallback<UserApi.Friend>(LOG_TAG) {
            @Override
            public void onNullResponse(Response<UserApi.Friend> response) {
                errorLog("Failed to add friend " + friend.id, null);
            }

            @Override
            public void onSuccessResponse(Response<UserApi.Friend> response) {
                updateCurrentUser();
            }
        });
    }

    private void log(final String message) {
        Log.d(LOG_TAG, message);
    }

    private void errorLog(String message, Throwable t) {
        Log.e(LOG_TAG, message, t);
    }
}
