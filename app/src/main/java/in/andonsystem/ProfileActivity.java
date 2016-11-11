package in.andonsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import in.andonsystem.database.DatabaseManager;
import in.andonsystem.services.Constants;
import in.andonsystem.services.DesgnService;

import in.andonsystem.R;
import com.splunk.mint.Mint;


public class ProfileActivity extends AppCompatActivity {

    private final String TAG = ProfileActivity.class.getSimpleName();
    private Context context;
    private SharedPreferences sharedPref;
    private String email;
    private String newEmail;
    private String mobile;
    private String newMobile;
    private String currPass;
    private String newPass;
    private String newPass2;
    private String authToken;

    private TextView user;
    private TextView userId;
    private TextView designation;
    private TextView mobileView;
    private TextView emailView;
    private ImageView emailIcon;
    private ImageView mobileIcon;
    private ImageView emailImageView;
    private ImageView mobileImageView;
    private ImageView emailImageSave;
    private ImageView mobileImageSave;
    private EditText emailEditText;
    private EditText mobileEditText;
    private EditText currPasswd;
    private EditText newPasswd;
    private EditText newPasswd2;
    private Button changePasswdBtn;
    private Button saveBtn;

    private LinearLayout container;
    private LinearLayout passwordLayout;
    private LinearLayout emailLayout;
    private LinearLayout mobileLayout;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(ProfileActivity.this, "544df31b");

        setContentView(R.layout.activity_profile);
        Log.i(TAG,"onCreate()");
        context = this;
        App.activity4 = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //View mapping
        emailLayout = (LinearLayout)findViewById(R.id.profile_email);
        mobileLayout = (LinearLayout)findViewById(R.id.profile_mobile);
        container = (LinearLayout)findViewById(R.id.profile_container_layout);
        user = (TextView)findViewById(R.id.profile_username);
        userId = (TextView)findViewById(R.id.profile_employee_id);
        designation = (TextView)findViewById(R.id.profile_designation);

        sharedPref = getSharedPreferences(Constants.PREF_FILE_NAME,0);
        authToken = sharedPref.getString(Constants.AUTHTOKEN,null);

        passwordLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        passwordLayout.setLayoutParams(params);
        int pixel1 = convertDpToPixel(70.0f);
        int pixel2 = convertDpToPixel(10.0f);
        params.setMargins(pixel1,pixel2,pixel1,pixel2);
        passwordLayout.setOrientation(LinearLayout.VERTICAL);
        container.addView(passwordLayout);


        emailView = createTextView();
        mobileView = createTextView();
        emailEditText = createEditText();
        mobileEditText = createEditText();
        emailImageView = createImageView();
        mobileImageView = createImageView();
        emailImageSave = createImageView();
        mobileImageSave = createImageView();
        emailIcon = createImageView();
        mobileIcon = createImageView();
        changePasswdBtn = createButton("change password");
        currPasswd = createEditText();
        newPasswd = createEditText();
        newPasswd2 = createEditText();
        saveBtn = createButton("save password");


        emailImageView.setBackgroundResource(R.drawable.ic_edit_white_24dp);
        mobileImageView.setBackgroundResource(R.drawable.ic_edit_white_24dp);
        emailImageSave.setBackgroundResource(R.drawable.ic_save_white_24dp);
        mobileImageSave.setBackgroundResource(R.drawable.ic_save_white_24dp);
        emailIcon.setBackgroundResource(R.drawable.ic_email_white_24dp);
        mobileIcon.setBackgroundResource(R.drawable.ic_phone_android_white_24dp);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart()");

        DesgnService desgnService = new DesgnService(DatabaseManager.getInstance().getDatabase());


        String username = sharedPref.getString(Constants.USERNAME,null);
        email = sharedPref.getString(Constants.EMAIL,null);
        String desgn = desgnService.getDesgnName(sharedPref.getInt(Constants.DESGN_ID,0));
        int employeeId = sharedPref.getInt(Constants.USERID,0);
        mobile = sharedPref.getString(Constants.MOBILE,null);

        Log.i(TAG,"email:" +email + " , mobile: " + mobile);

        user.setText(username);
        userId.setText(String.valueOf(employeeId));
        designation.setText(desgn);

        showEmailView();
        showMobileView();
        showPasswordBtn();


        emailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"emailView onClick()");
                showEmailEdit();
            }
        });
        mobileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"mobileView onClick()");
                showMobileEdit();
            }
        });

        emailImageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"emailSave onClick()");
                changeEmail();

            }
        });
        mobileImageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"mobileSave onClick()");
                changeMobile();
            }
        });

        changePasswdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordEdit();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

    }

    private void showEmailView(){
        emailLayout.removeAllViews();

        emailView.setText(email);

        emailLayout.addView(emailIcon);
        emailLayout.addView(emailView);
        emailLayout.addView(emailImageView);
    }

    private void showEmailEdit(){
        emailLayout.removeAllViews();

        emailEditText.setText(email);

        emailLayout.addView(emailIcon);
        emailLayout.addView(emailEditText);
        emailLayout.addView(emailImageSave);
    }

    private void showMobileView(){
        mobileLayout.removeAllViews();

        mobileView.setText(mobile);

        mobileLayout.addView(mobileIcon);
        mobileLayout.addView(mobileView);
        mobileLayout.addView(mobileImageView);
    }

    private void showMobileEdit(){
        mobileLayout.removeAllViews();

        mobileEditText.setText(mobile);
        mobileEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mobileEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mobileLayout.addView(mobileIcon);
        mobileLayout.addView(mobileEditText);
        mobileLayout.addView(mobileImageSave);
    }

    private void showPasswordBtn(){
        passwordLayout.removeView(changePasswdBtn);
        passwordLayout.removeView(currPasswd);
        passwordLayout.removeView(newPasswd);
        passwordLayout.removeView(newPasswd2);
        passwordLayout.removeView(saveBtn);
        passwordLayout.addView(changePasswdBtn);
    }

    private void showPasswordEdit(){
        passwordLayout.removeView(changePasswdBtn);


        currPasswd.setHint("current password");
        currPasswd.setHintTextColor(ContextCompat.getColor(context,R.color.white));
        currPasswd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswd.setHint("new password");
        newPasswd.setHintTextColor(ContextCompat.getColor(context,R.color.white));
        newPasswd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswd2.setHint("confirm new password");
        newPasswd2.setHintTextColor(ContextCompat.getColor(context,R.color.white));
        newPasswd2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        passwordLayout.addView(currPasswd);
        passwordLayout.addView(newPasswd);
        passwordLayout.addView(newPasswd2);
        passwordLayout.addView(saveBtn);
    }

    private void changeEmail(){
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");

        newEmail = emailEditText.getText().toString();
        if(newEmail.equals("")){
            Toast.makeText(context,"Email Field cannot be blank",Toast.LENGTH_SHORT).show();
        }else {
            pDialog.show();
            String url = "http://andonsystem.in/restapi/profile/change_email?authToken=" + authToken + "&email=" + newEmail;
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "Email change Response : " + response);
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                            if(response.contains("success")){
                                sharedPref.edit().putString(Constants.EMAIL,newEmail).commit();
                                Toast.makeText(context,"Email changed successfully.",Toast.LENGTH_SHORT).show();
                                onStart();
                            }

                            //If failed to change email, somehow login has benn changed (which is highly unlikely)
                            if (response.contains("fail")) {
                                sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
                                Intent i = new Intent(context,LoginActivity.class);
                                startActivity(i);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                            Log.d(TAG, error.toString());
                            Toast.makeText(context,"Slow Internet Connection,Unable to update changes.",Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        }

    }

    private void changeMobile(){
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");

        newMobile = mobileEditText.getText().toString();
        if(newMobile.equals("")){
            Toast.makeText(context,"Email Field cannot be blank",Toast.LENGTH_SHORT).show();
        }else {
            pDialog.show();
            String url = "http://andonsystem.in/restapi/profile/change_mobile?authToken=" + authToken + "&mobile=" + newMobile;
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "Email change Response : " + response);
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                            if(response.contains("success")){
                                sharedPref.edit().putString(Constants.MOBILE,newMobile).commit();
                                Toast.makeText(context,"Mobile number changed successfully.",Toast.LENGTH_SHORT).show();
                                onStart();
                            }

                            //If failed to change email, somehow login has benn changed (which is highly unlikely)
                            if (response.contains("fail")) {
                                sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
                                Intent i = new Intent(context,LoginActivity.class);
                                startActivity(i);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                            Log.d(TAG, error.toString());
                            Toast.makeText(context,"Slow Internet Connection,Unable to update changes.",Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        }

    }

    private void changePassword(){
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");

        currPass = currPasswd.getText().toString();
        newPass = newPasswd.getText().toString();
        newPass2 = newPasswd2.getText().toString();

        if(currPass.equals("") || newPass.equals("") || newPass2.equals("")){
            Toast.makeText(context,"Fields cannot be blank.",Toast.LENGTH_SHORT).show();
        }
        else if(!newPass.equals(newPass2)){
            Toast.makeText(context,"passwords do not match.",Toast.LENGTH_SHORT).show();
        }
        else {
            pDialog.show();
            String url = "http://andonsystem.in/restapi/profile/change_password?authToken=" + authToken + "&currPassword=" + currPass + "&newPassword=" + newPass;
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "Password change Response : " + response);
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                            if(response.contains("success")){
                                Toast.makeText(context,"password changed successfully.",Toast.LENGTH_SHORT).show();
                                onStart();
                            }

                            //If failed to change email, somehow login has benn changed (which is highly unlikely)
                            if (response.contains("fail")) {
                                Intent i = new Intent(context,LoginActivity.class);
                                startActivity(i);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                            Log.d(TAG, error.toString());
                            Toast.makeText(context,"Slow Internet Connection,Unable to update changes.",Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        }

    }

    private TextView createTextView(){
        TextView view = new TextView(context);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );
        param.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(param);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        int pixel = convertDpToPixel(5.0f);
        view.setPadding(pixel,pixel,pixel,pixel);
        view.setTextColor(ContextCompat.getColor(context,R.color.white));
        return view;
    }

    private EditText createEditText(){
        EditText view = new EditText(context);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );
        param.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(param);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        int pixel = convertDpToPixel(5.0f);
        view.setPadding(pixel,pixel,pixel,pixel);
        view.setTextColor(ContextCompat.getColor(context,R.color.white));
        return view;
    }

    private ImageView createImageView(){
        ImageView view = new ImageView(context);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        param.rightMargin = convertDpToPixel(10.0f);
        param.gravity = Gravity.CENTER_VERTICAL;
        view.setLayoutParams(param);

        return view;
    }

    private Button createButton(String text){
        Button button = new Button(context);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        param.gravity = Gravity.CENTER_HORIZONTAL;
        param.topMargin = convertDpToPixel(20.0f);
        param.bottomMargin = convertDpToPixel(150.0f);
        button.setLayoutParams(param);
        button.setText(text);
        //button.setBackgroundColor(Color.parseColor("#0000"));
        int pixel = convertDpToPixel(5.0f);
        button.setPadding(3*pixel,pixel,3*pixel,pixel);
        button.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary));
        button.setTextColor(ContextCompat.getColor(context,R.color.white));
        return button;
    }

    private int convertDpToPixel(float dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG,"finish()");
        App.activity4 = null;
    }
}
