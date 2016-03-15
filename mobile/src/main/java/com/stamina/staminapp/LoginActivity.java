package com.stamina.staminapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private UserLoginTask mAuthTask = null; // Keep track of the login task to ensure we can cancel it if requested.

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mIpView;
    private View mProgressView;
    private View mLoginFormView;
    private boolean DEBUG = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mIpView = (EditText) findViewById(R.id.ip);
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private boolean isEmailValid(String email) {
        return true;//email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return true;//password.length() > 4;
    }

    private boolean isIpValid(String ip){
        if (DEBUG){return true;}
        int dots = 0, pos = 0;
        String temp;
        for( int i=0; i<ip.length(); i++ ) {
            if (ip.charAt(i) == '.') {
                dots++;
                temp = ip.substring(pos, i);
                try {
                    Integer.parseInt(temp);
                } catch (NumberFormatException e) {
                    return false;
                }
                pos = i + 1;
            }
        }
        if(dots != 3){
            return false;
        } else {
            temp = ip.substring(pos,ip.length());
            try{
                Integer.parseInt(temp);
            } catch (NumberFormatException e){
                return false;
            }
            return true;
        }
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        mEmailView.setError(null);
        mPasswordView.setError(null);
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String ip = mIpView.getText().toString();
        View focusView = null;
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("This field can't be empty");
            focusView = mEmailView;
        }
        else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("This field can't be empty");
            focusView = mPasswordView;
        }
        else if  (TextUtils.isEmpty(ip)) {
            mIpView.setError("This field can't be empty");
            focusView = mIpView;
        }
        else if (!isEmailValid(email)) {
            mEmailView.setError("The format of the email address is incorrect");
            focusView = mEmailView;
        }
        else if (!isPasswordValid(password)) {
            mPasswordView.setError("This password is too short");
            focusView = mPasswordView;
        }
        else if (!isIpValid(ip)){
            mIpView.setError("The format of the IP address is incorrect");
            focusView = mIpView;
        }
        if(focusView != null){
            focusView.requestFocus();
            return;
        }
        // Show a progress spinner, and kick off a background task to perform the user login attempt.
        showProgress(true);
        mAuthTask = new UserLoginTask(email, password, ip);
        mAuthTask.execute((Void) null);
    }



    //Shows the progress UI and hides the login form.
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? (float)0.76 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public class UserLoginTask extends AsyncTask<Void, Void, String> {
        private final String mEmail;
        private final String mPassword;
        private Networking net = new Networking(null, "192.168.1.1");

        UserLoginTask(String email, String password, String ip) { //constructor
            mEmail = email;
            mPassword = password;
            net.set_url(ip);
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                Thread.sleep(150);
            } catch (InterruptedException e){}
            String page = "login", response;
            Map<String, String> urlparameters = new HashMap<String, String>();
            urlparameters.put("username",mEmail);
            urlparameters.put("password",mPassword);
            response = net.post(page, urlparameters);
            if (response == null){
                if (DEBUG){return "auth_success";}
                return "connection_fail";
            } else if (response.equals("true")) { //HERE
                return "auth_success"; //then eventually propose to register
            }
            return "auth_fail";
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);
            if (result.equals("auth_success")) {
                String cookie = net.get_cookie();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("cookie", cookie);
                intent.putExtra("source", "LoginActivity");
                startActivity(intent);
            } else if (result.equals("auth_fail")) {
                mPasswordView.setError("Wrong email/password combination");
                mPasswordView.requestFocus();
            } else{
                Toast.makeText(getApplicationContext(), "Connection to server failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

