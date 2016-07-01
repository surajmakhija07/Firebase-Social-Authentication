package firebaseauthentication.android.com.firebaseauthentication.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import firebaseauthentication.android.com.firebaseauthentication.R;
import io.fabric.sdk.android.Fabric;
import utils.DialogUtils;
import utils.LogUtil;
import utils.SharedPreferenceUtils;

public class SigninSignupActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "SigninSignupActivity";
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1;
    GoogleApiClient mGoogleApiClient;
    static LoginManager loginManager;
    CallbackManager callbackManager;
    private ArrayList<String> permissions = new ArrayList<>();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.login_facebook_login_button)
    LoginButton mFacebookLoginButton;

    @BindView(R.id.login_twitter_login_button)
    TwitterLoginButton mTwitterLoginButton;

    @OnClick(R.id.login_sign_in_button)
    public void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        DialogUtils.showProgressDialog(SigninSignupActivity.this, "", getString(R.string.sign_in), false);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SigninSignupActivity.this, "Google sign in failed. Please try again",
                                    Toast.LENGTH_SHORT).show();
                            DialogUtils.dismissProgressDialog();
                        } else {
                            Intent intent = new Intent(SigninSignupActivity.this, UserProfileActivity.class);
                            SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("account", "google");
                            SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("username", acct.getDisplayName());
                            SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("googleId", acct.getId());
                            startActivity(intent);
                            finish();
                            DialogUtils.dismissProgressDialog();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                Log.d("google sign in", "successful");
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.e("google sign in", "failed");
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_signup);
        ButterKnife.bind(this);
        mToolbar.setTitle("Firebase Authentication");

        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();
        permissions.add("user_friends");
        permissions.add("public_profile");
        permissions.add("email");
        mFacebookLoginButton.setReadPermissions(permissions);
        /*try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "firebaseauthentication.android.com.firebaseauthentication",
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("exception", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("exception", e.toString());
        }*/

        mFacebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("facebook login", "success");
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        String name = object.optString("name");
                        String facebookUserId = object.optString("id");
                        if (!TextUtils.isEmpty(name)) {
                            SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("username", name);
                        }
                        SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("facebookUserId", facebookUserId);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SigninSignupActivity.this, "Facebook Sign in failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        });

        mTwitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d("Twitter Login", "success");

                String name = result.data.getUserName();
                String twitterUserId = String.valueOf(result.data.getUserId());

                if (!TextUtils.isEmpty(name)) {
                    SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("username", name);
                }
                SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("twitterUserId", twitterUserId);

                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e("Twitter Login", "failure");
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.e(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                    SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("uid", user.getUid());
                } else {
                    // User is signed out
                    Log.e(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // client id : 495284022331-4d4e0em62gc3upchj8vecrmks7k6ai58.apps.googleusercontent.com
        // client secret : Xx8Q9G8VG30ow__ooiSyvb3u
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        DialogUtils.showProgressDialog(SigninSignupActivity.this, "", getString(R.string.sign_in), false);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SigninSignupActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Login with Facebook successful");
                            Intent intent = new Intent(SigninSignupActivity.this, UserProfileActivity.class);
                            SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("account", "facebook");
                            startActivity(intent);
                            finish();
                        }
                        DialogUtils.dismissProgressDialog();
                    }
                });
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        // [START_EXCLUDE silent]
        DialogUtils.showProgressDialog(SigninSignupActivity.this, "", getString(R.string.sign_in), false);
        // [END_EXCLUDE]

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential Twitter : onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SigninSignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(SigninSignupActivity.this, UserProfileActivity.class);
                            SharedPreferenceUtils.getInstance(SigninSignupActivity.this).setValue("account", "twitter");
                            startActivity(intent);
                            finish();
                        }
                        DialogUtils.dismissProgressDialog();
                    }
                });
    }
}
