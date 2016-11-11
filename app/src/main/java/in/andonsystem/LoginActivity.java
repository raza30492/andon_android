package in.andonsystem;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import in.andonsystem.services.Constants;
import in.andonsystem.services.MiscService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity{

    private String TAG = LoginActivity.class.getSimpleName();

    private LinearLayout container;
    private EditText userId;
    private EditText password;

    private ProgressDialog pDialog;
    private SharedPreferences sharedPref;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(LoginActivity.this, "544df31b");
        setContentView(R.layout.activity_login);
        Log.i(TAG,"onCreate()");
        App.activity2 = this;
        context = this;

        container = (LinearLayout)findViewById(R.id.login_container);
        userId = (EditText)findViewById(R.id.userId);
        password = (EditText)findViewById(R.id.password);
        sharedPref = this.getSharedPreferences(Constants.PREF_FILE_NAME,0);

        Boolean loggedIn = sharedPref.getBoolean(Constants.LOGGED_IN,false);

        if(loggedIn){
            goToHome();
        }

    }

    public void signIn(View v){

        if(userId.getText().toString().equals("")){
            Snackbar.make(container,"Enter Employee Id.",Snackbar.LENGTH_SHORT).show();
        }
        else if(password.getText().toString().equals("")){
            Snackbar.make(container,"Enter Password.",Snackbar.LENGTH_SHORT).show();
        }else{
            int id = Integer.parseInt(userId.getText().toString());
            String passwd = password.getText().toString();
            Boolean isConnected = MiscService.isConnectedToInternet(context);
            if(isConnected) {
                logIn(id, passwd);
            }else{
                Snackbar.make(container,"Check your Internet Connection.",Snackbar.LENGTH_LONG).show();
            }
        }

    }

    public void forgotPassword(View v){
        Intent intent = new Intent(context,ForgptPasswordActivity.class);
        startActivity(intent);
    }

    private void logIn(int id,String passwd){
        String url = "http://andonsystem.in/restapi/res/auth?userId=" + id + "&password=" + passwd;

        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG,"Login Response :" + response.toString());
                if(pDialog != null & pDialog.isShowing()){
                    pDialog.dismiss();
                }
                try {
                    String code = response.getString("code");
                    if (code.contains("success")) {
                        JSONObject user = response.getJSONObject("data");
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putInt(Constants.USERID, user.getInt("userId"));
                        editor.putString(Constants.USERNAME, user.getString("username"));
                        editor.putString(Constants.EMAIL, user.getString("email"));
                        editor.putInt(Constants.LEVEL, user.getInt("level"));
                        editor.putInt(Constants.DESGN_ID, user.getInt("desgnId"));
                        editor.putString(Constants.MOBILE, user.getString("mobile"));
                        editor.putString(Constants.AUTHTOKEN,user.getString("authToken"));
                        editor.putBoolean(Constants.LOGGED_IN,true);
                        editor.commit();
                        goToHome();

                    } else {
                        Snackbar.make(container,"Incorrect Employee Id or Password, Try again!",Snackbar.LENGTH_SHORT).show();
                    }

                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(pDialog != null & pDialog.isShowing()){
                    pDialog.dismiss();
                }
                Snackbar.make(container,"Check your Internet Connection.",Snackbar.LENGTH_SHORT).show();
            }
        };

        Log.i(TAG,"Login url: " + url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url,null,listener,errorListener);
        //RequestJsonObject loginRequest = new RequestJsonObject(Request.Method.POST,url,params,null,listener,errorListener);

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

    }

    private void goToHome(){
        Intent i = new Intent(this,HomeActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        App.close();
    }

    @Override
    protected void onDestroy() {
        App.activity2 = null;
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG,"finish()");
    }
}

/*
JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG,"Login Response :" + response.toString());
                        if(pDialog != null & pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        try {
                            String code = response.getString("code");
                            if (code.contains("success")) {
                                JSONObject user = response.getJSONObject("data");
                                SharedPreferences.Editor editor = sharedPref.edit();

                                editor.putInt(Constants.USERID, user.getInt("userId"));
                                editor.putString(Constants.USERNAME, user.getString("username"));
                                editor.putString(Constants.EMAIL, user.getString("email"));
                                editor.putInt(Constants.LEVEL, user.getInt("level"));
                                editor.putInt(Constants.DESGN_ID, user.getInt("desgnId"));
                                editor.putString(Constants.MOBILE, user.getString("mobile"));
                                editor.putString(Constants.AUTHTOKEN,user.getString("authToken"));
                                editor.putBoolean(Constants.LOGGED_IN,true);
                                editor.commit();
                                goToHome();

                            } else {
                                Snackbar.make(container,"Incorrect Employee Id or Password, Try again!",Snackbar.LENGTH_SHORT).show();
                            }

                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(pDialog != null & pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        Snackbar.make(container,"Check your Internet Connection.",Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
*/
