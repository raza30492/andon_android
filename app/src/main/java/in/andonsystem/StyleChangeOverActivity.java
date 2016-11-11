package in.andonsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import in.andonsystem.database.DatabaseManager;
import in.andonsystem.request.RequestString;
import in.andonsystem.services.Constants;
import in.andonsystem.services.UserService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import java.util.HashMap;
import java.util.Map;

public class StyleChangeOverActivity extends AppCompatActivity {

    private final String TAG = StyleChangeOverActivity.class.getSimpleName();
    private Context context;
    private SharedPreferences sharedPref;
    private Spinner line;
    private EditText from;
    private EditText to;
    private EditText remarks;

    private String fromStr,toStr,remarksStr,lineStr;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(StyleChangeOverActivity.this, "544df31b");
        setContentView(R.layout.activity_style_change_over);
        Log.i(TAG,"onCreate()");
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //View mapping
        line = (Spinner)findViewById(R.id.style_lines);
        from = (EditText)findViewById(R.id.style_from);
        to = (EditText)findViewById(R.id.style_to);
        remarks = (EditText)findViewById(R.id.style_remarks);
        sharedPref = getSharedPreferences(Constants.PREF_FILE_NAME,0);


        int noOfLines = sharedPref.getInt(Constants.LINES,0);
        String[] lineArray = new String[noOfLines];
        for(int i = 0; i < lineArray.length; i++){
            lineArray[i] = "Line " + (i+1);
        }

        ArrayAdapter<String> lineAdapter = new ArrayAdapter<>(this,R.layout.spinner_list_item,R.id.spinner_item,lineArray);
        lineAdapter.setDropDownViewResource(R.layout.spinner_list_item);
        line.setAdapter(lineAdapter);
    }

    public void submit(View view){
        RequestQueue queue = AppController.getInstance().getRequestQueue();

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");

        lineStr = ((TextView)line.findViewById(R.id.spinner_item)).getText().toString();
        toStr = to.getText().toString();
        fromStr = from.getText().toString();
        remarksStr = remarks.getText().toString();

        int userId = sharedPref.getInt(Constants.USERID,0);
        final String userName = (new UserService(DatabaseManager.getInstance().getDatabase())).getUserName(userId);

        if(fromStr.equals("") || toStr.equals("") || remarksStr.equals("")){
            Toast.makeText(context,"Fields cannot be blank",Toast.LENGTH_SHORT).show();
        }else{
            pDialog.show();

            String url = "http://andonsystem.in/restapi/style_changeover";
            Map<String,String> param = new HashMap<>();
            param.put("line", lineStr.split(" ")[1]);
            param.put("from", fromStr);
            param.put("to", toStr);
            param.put("remarks", remarksStr);
            param.put("submitBy", userName);

            Response.Listener<String> listener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG, "style changeover Response : " + response);
                    if(pDialog != null && pDialog.isShowing()){
                        pDialog.dismiss();
                    }
                    if(response.contains("success")){
                        Toast.makeText(context,"Style changeover submitted successfully.",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            };
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(pDialog != null && pDialog.isShowing()){
                        pDialog.dismiss();
                    }
                    Log.d(TAG, error.toString());
                    Toast.makeText(context,"Slow Internet Connection,Unable to submit style changeover.",Toast.LENGTH_SHORT).show();
                }
            };
            Log.i(TAG,"style changeover request url :" + url);

            RequestString request = new RequestString(url,param,listener,errorListener);
            request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
        }

    }



}
