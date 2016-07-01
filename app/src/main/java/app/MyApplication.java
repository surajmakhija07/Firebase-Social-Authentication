package app;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import firebaseauthentication.android.com.firebaseauthentication.R;
import io.fabric.sdk.android.Fabric;

/**
 * Created by comp-1 on 29/6/16.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(this);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.str_twitter_key), getString(R.string.str_twitter_secret));
        Fabric.with(this, new Twitter(authConfig));
    }
}
