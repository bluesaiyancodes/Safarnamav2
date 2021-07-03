package com.strongties.app.safarnama;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.strongties.app.safarnama.classes.User;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
        public static final String TAG = "LoginActivity";

        static final int GOOGLE_SIGN =123;
        FirebaseAuth firebaseAuth;
        Button login_btn;

        ProgressBar progressBar;
        GoogleSignInClient googleSignInClient;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login_screen);


            progressBar = findViewById(R.id.login_progressbar);
            login_btn = findViewById(R.id.login_btn);
            TextView tv_login = findViewById(R.id.login_text);

            login_btn.setVisibility(View.GONE);
            tv_login.setVisibility(View.GONE);

            // Initializing Firebase Authentication
            firebaseAuth = FirebaseAuth.getInstance();




            // Signin Options Builder
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                    .Builder()
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            // Google signin Client
            googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

            login_btn.setOnClickListener(v -> LoginActivity.this.SignInGoogle());

            // If User exists in Database
            if(firebaseAuth.getCurrentUser() != null) {
                DocumentReference oldUserRef = FirebaseFirestore.getInstance()
                        .collection(getString(R.string.collection_users))
                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                oldUserRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            User user1 = Objects.requireNonNull(task.getResult()).toObject(User.class);
                            assert user1 != null;
                            // Set last login to null, so that server time is automatically assigned
                            user1.setLastlogin(null);
                            // Set updated user at database
                            oldUserRef.set(user1);
                            // Launch Next activity
                            Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                            LoginActivity.this.startActivity(myIntent);
                            LoginActivity.this.finish();

                        }
                    }
                });


            } else {
                // if user doesn't exist. Show sign in Options
                tv_login.setVisibility(View.VISIBLE);
                login_btn.setVisibility(View.VISIBLE);
            }



    }

    void SignInGoogle(){
        progressBar.setVisibility(View.VISIBLE);
        Intent signinIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signinIntent, GOOGLE_SIGN);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN){
            Task<GoogleSignInAccount> task = GoogleSignIn
                    .getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account != null){
                    firebaseAuthwithGoogle(account);
                }

            }catch (ApiException e){
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthwithGoogle(GoogleSignInAccount account){
        Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("TAG", "signInWithCredential:success");

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            LoginActivity.this.writeFirebase(user);


                            Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                            // myIntent.putExtra("key", value); //Optional parameters
                            LoginActivity.this.startActivity(myIntent);
                            LoginActivity.this.finish();

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("TAG", "signInWithCredential:Failure", task.getException());


                            Toast.makeText(LoginActivity.this, "Signin Failed", Toast.LENGTH_SHORT).show();


                        }
                    }
                });

    }

    private void writeFirebase(FirebaseUser user) {
        if(user != null) {
            String email = user.getEmail();
            assert email != null;
            String name = email.substring(0, email.indexOf("@"));
            String photo = String.valueOf(user.getPhotoUrl());
            String uid = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Create a new user
            User user_ = new User();
            user_.setUsername(name);
            user_.setEmail(email);
            user_.setPhoto(photo);
            user_.setUser_id(uid);
            user_.setAvatar(getString(R.string.avatar_0));
            user_.setDateofjoin(null);
            user_.setLastlogin(null);

            // Create a document reference to add user to the database
            DocumentReference newUserRef = db
                    .collection(getString(R.string.collection_users))
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

            // New User
            newUserRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        //do nothing
                        User user1 = documentSnapshot.toObject(User.class);
                        assert user1 != null;
                        user1.setLastlogin(null);
                        DocumentReference docRef = db
                                .collection(getString(R.string.collection_users))
                                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                        docRef.set(user1).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // do nothing
                            }
                        });
                    } else {
                        newUserRef.set(user_);
                    }
                }
            });
        }
    }

    private void Logout() {
        FirebaseAuth.getInstance().signOut();
        googleSignInClient.signOut();
    }
}
