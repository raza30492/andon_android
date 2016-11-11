package in.andonsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import in.andonsystem.request.RequestString;
import in.andonsystem.services.MiscService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import java.util.HashMap;
import java.util.Map;

public class ForgptPasswordActivity extends AppCompatActivity {

    private final String TAG = ForgptPasswordActivity.class.getSimpleName();
    private Context context;
    private LinearLayout container;
    private ProgressDialog pDialog;
    private RequestQueue queue;

    private Button submit1;
    private Button submit2;
    private Button submit3;
    private TextView title2;
    private TextView title3;
    private TextView error1;
    private TextView error2;
    private EditText empId;
    private EditText otp;
    private EditText newPasswd;
    private EditText newPasswd2;

    Response.ErrorListener errorListener;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(ForgptPasswordActivity.this, "544df31b");
        setContentView(R.layout.activity_forgpt_password);
        Log.i(TAG,"onCreate()");
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        container = (LinearLayout) findViewById(R.id.forgot_password_container);

        //ErrorListener
        errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,"Error: " + error.toString());
                if(pDialog != null && pDialog.isShowing()){
                    pDialog.dismiss();
                }
                Snackbar.make(container,"Slow Internet Connection, Retry.",Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(context,"Slow Internet Connection, Retry.",Toast.LENGTH_SHORT).show();
            }
        };
        //queue = AppController.getInstance().getRequestQueue();

        submit1 = createButton("Submit");
        submit2 = createButton("Submit");
        submit3 = createButton("Submit");
        title2 = createTextView("OTP has been sent to your registered mobile number",30);
        //error1 = createTextView("User not")
        error2 = createTextView("Incorrect OTP entered,Retry.",30);
        empId = createEditText("Enter Employee Id");
        otp = createEditText("Enter 6 digit OTP");
        newPasswd = createEditText("new password");
        newPasswd2 = createEditText("confirm new password");

        otp.setInputType(InputType.TYPE_CLASS_NUMBER);
        otp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        empId.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        newPasswd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswd2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        container.removeAllViews();
        container.addView(empId);
        container.addView(submit1);

        submit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String empIdText = empId.getText().toString();

                if(empIdText.equals("")){
                    Toast.makeText(context,"Enter Employee Id first.",Toast.LENGTH_SHORT).show();
                }
                else if(empIdText.length() != 5){
                    Toast.makeText(context,"Employee Id must be 5 digit number",Toast.LENGTH_SHORT).show();
                }
                else{
                    userId = Integer.parseInt(empIdText);
                    sendOTP();
                }
            }
        });

        submit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otpText = otp.getText().toString();
                if(otpText.equals("") ){
                    Toast.makeText(context,"Enter OTP first",Toast.LENGTH_SHORT).show();
                }else if(otpText.length() != 6){
                    Toast.makeText(context,"OTP must be 6 digit number. ",Toast.LENGTH_SHORT).show();
                }else {
                    int otp = Integer.parseInt(otpText);
                    verifyOTP(otp);
                }

            }
        });
        submit3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass1 = newPasswd.getText().toString();
                String pass2 = newPasswd2.getText().toString();
                if(pass1.equals("") || pass2.equals("")){
                    Toast.makeText(context,"Password Field cannot be blank.",Toast.LENGTH_SHORT).show();
                }else if(!pass1.equals(pass2)){
                    Toast.makeText(context,"Passwords do not match.",Toast.LENGTH_SHORT).show();
                }else{
                    changePassword(pass1);
                }
            }
        });


    }

    private void sendOTP(){
        Log.i(TAG,"sendOTP()");
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/forgot_passwd/send_otp";
        Map<String, String> param = new HashMap<>();
        param.put("userId",String.valueOf(userId));

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"send otp response:" + response);
                if(pDialog != null & pDialog.isShowing()){
                    pDialog.dismiss();
                }

                if(response.contains("success")){
                    container.removeAllViews();
                    container.addView(title2);
                    container.addView(otp);
                    container.addView(submit2);
                }
                else{
                    Toast.makeText(context,"User not found.",Toast.LENGTH_SHORT).show();
                }
            }
        };

        RequestString request = new RequestString(url,param,listener,errorListener);
        request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().getRequestQueue().add(request);
    }

    private void verifyOTP(int otp){
        Log.i(TAG,"verifyOTP()");
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/forgot_passwd/verify_otp";
        Map<String, String> param = new HashMap<>();
        param.put("userId",String.valueOf(userId));
        param.put("otp",String.valueOf(otp));

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"verify otp response:" + response);
                if(pDialog != null & pDialog.isShowing()){
                    pDialog.dismiss();
                }

                if(response.contains("success")){
                    container.removeAllViews();
                    container.addView(newPasswd);
                    container.addView(newPasswd2);
                    container.addView(submit3);
                }
                else{
                    Toast.makeText(context,"Entered Incorrect OTP",Toast.LENGTH_SHORT).show();
                }
            }
        };

        RequestString request = new RequestString(url,param,listener,errorListener);
        request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().getRequestQueue().add(request);
    }

    private void changePassword(String password){
        Log.i(TAG,"changePassword()");
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/forgot_passwd/change_password";
        Map<String, String> param = new HashMap<>();
        param.put("userId",String.valueOf(userId));
        param.put("newPassword",password);

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"change password response:" + response);
                if(pDialog != null & pDialog.isShowing()){
                    pDialog.dismiss();
                }
                if(response.contains("success")){
                    Toast.makeText(context,"Password reset successfully.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    Toast.makeText(context,"Some Error occured, Try again.",Toast.LENGTH_SHORT).show();
                }
            }
        };

        RequestString request = new RequestString(url,param,listener,errorListener);
        request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().getRequestQueue().add(request);
    }


    private EditText createEditText(String hint){
        EditText editText = new EditText(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editText.setLayoutParams(params);
        editText.setHint(hint);
        return editText;
    }
    private TextView createTextView(String text,float margin){
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.bottomMargin = MiscService.convertDpToPixel(margin);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        return textView;
    }
    private Button createButton(String text){
        Button button = new Button(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        //params.gravity = Gravity.CENTER_HORIZONTAL;
        button.setLayoutParams(params);
        button.setText(text);
        return button;
    }

}
