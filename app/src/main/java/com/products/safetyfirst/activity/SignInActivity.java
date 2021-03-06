package com.products.safetyfirst.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.products.safetyfirst.R;
import com.products.safetyfirst.models.UserModel;

import java.util.HashMap;

import static com.products.safetyfirst.utils.DatabaseUtil.getDatabase;

public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    public static final String PREF_KEY_FIRST_START = "com.vikas.dtu.safetyfirst2.PREF_KEY_FIRST_START";
    public static final int REQUEST_CODE_INTRO = 1;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1;
    public static GoogleApiClient mGoogleApiClient;
    public static FirebaseAuth mFirebaseAuth;
    public static boolean signin = false;
    private SignInButton mGoogleSignInButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button customSigninButton;
    private TextView mSignUpText;
    private SignInButton mSignInButton;
    private Button mSkipButton;

    public static boolean isNetworkStatusAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                if (netInfos.isConnected())
                    return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);
        mDatabase = getDatabase().getReference();

        // Assign fields
        mGoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        customSigninButton = (Button) findViewById(R.id.button_sign_in);
        mSkipButton = (Button) findViewById(R.id.button_skip);
        //  mSignUpButton = (Button) findViewById(R.id.button_sign_up);

      //  mSignUpText = (TextView) findViewById(R.id.create_account);



        // Click listeners
        customSigninButton.setOnClickListener(this);
        mSkipButton.setOnClickListener(this);
       // mSignUpText.setOnClickListener(this);
        // Set click listeners


        mGoogleSignInButton.setOnClickListener(this);



        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.e(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                     isNewUser(user.getUid(), user.getDisplayName(), user.getEmail(), String.valueOf(user.getPhotoUrl()));

                } else {
                    // User is signed out
                    Log.e(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }


    private void isNewUser(final String userId, final String name, final String email, final String image){

        DatabaseReference userRef = getDatabase().getReference().child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // TODO: handle the case where the data already exists
                    Toast.makeText(SignInActivity.this, "Welcome Back", Toast.LENGTH_SHORT).show();
                }
                else {
                    // TODO: handle the case where the data does not yet exist
                    writeNewUser(userId, name, email, image);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signin = true;
                if(isNetworkStatusAvailable(SignInActivity.this)){
                    googleSignIn();}
                else{
                    Toast.makeText(SignInActivity.this,"Please check your Internet Connection!!",Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.button_skip:
               // Toast.makeText(this, "Skip", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignInActivity.this, HomeActivity.class));

                finish();
                break;

            case R.id.button_sign_in:
                signIn();
                break;
        }
    }

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        }else {
            mPasswordField.setError(null);
        }

        if (mPasswordField.getText().toString().length()<6){
            mPasswordField.setError("Password Length should be greater than 6");
            result = false;
        }else {
            mPasswordField.setError(null);
        }

        return result;
    }

    private void googleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                Log.d("google sign in", "successful");
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
            }
        }
        else  if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(PREF_KEY_FIRST_START, false)
                        .apply();
            }
            else {
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(PREF_KEY_FIRST_START, true)
                        .apply();
                //User cancelled the intro so we'll finish this activity too.
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (signin)
            Toast.makeText(this, "You must sign in first",
                    Toast.LENGTH_LONG).show();

        else
            super.onBackPressed();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
       // DialogUtils.showProgressDialog(SignInActivity.this, "", getString(R.string.sign_in), false);
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                           // DialogUtils.dismissProgressDialog();
                            hideProgressDialog();
                        } else {
                            onAuthSuccess(mFirebaseAuth.getCurrentUser());
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
    private void writeNewUser(String userId, String name, String email, String image) {
        UserModel user = new UserModel(name, email, image);

        HashMap<String, Boolean> mListOfInterests = new HashMap<>();

        mListOfInterests.put("PPE", false);
        mListOfInterests.put("Fire Safety", false);
        mListOfInterests.put("Ladder Safety", false);
        mListOfInterests.put("Health Safety", false);
        mListOfInterests.put("Chemical", false);
        mListOfInterests.put("Others", false);

        mDatabase.child("users").child(userId).setValue(user);
        mDatabase.child("user-interests").child(userId).setValue(mListOfInterests);
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username;
        if(user.getDisplayName()==null){
            username = usernameFromEmail(user.getEmail());
        }
        else username = user.getDisplayName();
        // String username = mNameField.getText().toString();
        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail(), user.getPhotoUrl()!= null ?user.getPhotoUrl().toString():null);

        // Go to DashboardnActivity
        startActivity(new Intent(SignInActivity.this, HomeActivity.class));
        finish();
       // DialogUtils.dismissProgressDialog();
        hideProgressDialog();
    }






    public void forgotPassword(View view) {
        startActivity(new Intent(this,PasswordResetActivity.class));
    }

    public void createAccount(View view) {
        startActivity(new Intent(this,SignUpActivity.class));
        finish();
    }
}