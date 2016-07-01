package firebaseauthentication.android.com.firebaseauthentication.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.SessionManager;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import firebaseauthentication.android.com.firebaseauthentication.R;
import utils.SharedPreferenceUtils;

public class UserProfileActivity extends AppCompatActivity {

    @BindView(R.id.textViewUsername)
    TextView textViewUsername;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        textViewUsername.setText(getString(R.string.str_welcome) + SharedPreferenceUtils.getInstance(this).getStringValue("username", ""));

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("google connection", "onConnectionFailed:" + connectionResult);
                        Toast.makeText(UserProfileActivity.this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();

                if (SharedPreferenceUtils.getInstance(this).getStringValue("account", "").equals("google")) {
                    Log.e("signout", "google");
                    // Google sign out
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                SharedPreferenceUtils.getInstance(UserProfileActivity.this).clear();
                            }
                        }
                    });
                } else if (SharedPreferenceUtils.getInstance(this).getStringValue("account", "").equals("facebook")) {
                    Log.e("signout", "facebook");
                    LoginManager.getInstance().logOut();
                    SharedPreferenceUtils.getInstance(UserProfileActivity.this).clear();
                } else if (SharedPreferenceUtils.getInstance(this).getStringValue("account", "").equals("twitter")) {
                    Log.e("signout", "twitter");
                    Twitter.logOut();
                    SharedPreferenceUtils.getInstance(UserProfileActivity.this).clear();
                }

                Intent intent = new Intent(this, SigninSignupActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
