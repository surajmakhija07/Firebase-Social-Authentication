package firebaseauthentication.android.com.firebaseauthentication.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import firebaseauthentication.android.com.firebaseauthentication.R;
import io.fabric.sdk.android.Fabric;
import utils.SharedPreferenceUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent;
        if (!SharedPreferenceUtils.getInstance(this).getStringValue("googleId", "").equals("")) {
            intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (!SharedPreferenceUtils.getInstance(this).getStringValue("facebookUserId", "").equals("")) {
            intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (!SharedPreferenceUtils.getInstance(this).getStringValue("twitterUserId", "").equals("")) {
            intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(this, SigninSignupActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
