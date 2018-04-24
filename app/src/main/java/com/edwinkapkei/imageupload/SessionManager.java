package com.edwinkapkei.imageupload;

import android.content.Context;
import android.content.SharedPreferences;

class SessionManager {

    private static final String IMAGE_URL = "IMAGE_URL";

    private static final String KEY_URL = "IMAGE_URL.url";

    private final Context mContext;

    public SessionManager(Context context) {
        mContext = context;
    }

    public String getUrl() {
        SharedPreferences preferences = mContext.getSharedPreferences(IMAGE_URL, Context.MODE_PRIVATE);
        return preferences.getString(KEY_URL, "hello");
    }

    public void setUrl(String url) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(IMAGE_URL, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_URL, url);
        editor.apply();
    }
}

