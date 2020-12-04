package ru.mail.z_team.icon_fragments;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class DatabaseCallback<T> implements Callback<T> {

    private static final int FAILED_TO_READ_WRITE_DB_CODE = 401;
    private final String logTag;

    public abstract void onNull(Response<T> response);

    public abstract void onSuccessResponse(Response<T> response);

    protected DatabaseCallback(final String logTag) {
        this.logTag = logTag;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.code() == FAILED_TO_READ_WRITE_DB_CODE) {
            Log.e(logTag, "Problem with Auth", null);
            return;
        }
        if (response.body() == null) {
            Log.e(logTag, "File not found", null);
            onNull(response);
            return;
        }
        if (response.isSuccessful()) {
            onSuccessResponse(response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        Log.e(logTag, "Failed to load", t);
    }
}
