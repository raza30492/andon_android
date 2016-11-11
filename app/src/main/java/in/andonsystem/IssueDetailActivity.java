package in.andonsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import in.andonsystem.database.DatabaseSchema;
import in.andonsystem.request.RequestString;
import in.andonsystem.services.Constants;
import in.andonsystem.services.DeptService;
import in.andonsystem.services.DesgnService;
import in.andonsystem.services.IssueService;
import in.andonsystem.services.ProblemService;
import in.andonsystem.services.SectionService;
import in.andonsystem.services.UserService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class IssueDetailActivity extends AppCompatActivity {

    private final String TAG = IssueDetailActivity.class.getSimpleName();
    private Context context;
    private SharedPreferences sharedPref;
    private SQLiteDatabase db;
    private DateFormat df;
    private SimpleDateFormat sdf;

    private TextView problem;
    private TextView dept;
    private TextView section;
    private TextView line;
    private TextView opNo;
    private TextView raisedAt;
    private TextView ackAt;
    private TextView fixAt;
    private TextView raisedBy;
    private TextView ackBy;
    private TextView fixBy;
    private TextView desc;
    private TextView processingAt;
    private Button ackButton;
    private Button seekHelpBtn;
    private Button fixButton;
    private LinearLayout layout;

    private String raiseTime;
    private String ackTime;
    private String solveTime;
    private String authToken;
    private int userId;
    private int issueId;
    private int userLevel;
    private int ackby;
    private int fixby;
    private ProgressDialog pDialog;

    private Response.ErrorListener errorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(IssueDetailActivity.this, "544df31b");
        setContentView(R.layout.activity_issue_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.i(TAG,"onCreate()");
        context = this;
        //ErrorListener
        errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,"Error: " + error.toString());
                if(pDialog != null && pDialog.isShowing()){
                    pDialog.dismiss();
                }
                Toast.makeText(context,"Check your Internet Connection.",Toast.LENGTH_SHORT).show();
            }
        };

        //View Mapping
        sharedPref = getSharedPreferences(Constants.PREF_FILE_NAME,0);
        authToken = sharedPref.getString(Constants.AUTHTOKEN,null);
        userId = sharedPref.getInt(Constants.USERID,0);

        problem = (TextView)findViewById(R.id.detail_prob);
        dept = (TextView)findViewById(R.id.detail_dept);
        section = (TextView)findViewById(R.id.detail_section);
        line = (TextView)findViewById(R.id.detail_line);
        opNo = (TextView)findViewById(R.id.detail_op_no);
        raisedAt = (TextView)findViewById(R.id.detail_raised_at);
        ackAt = (TextView)findViewById(R.id.detail_ack_at);
        fixAt = (TextView)findViewById(R.id.detail_solved_at);
        raisedBy = (TextView)findViewById(R.id.detail_raised_by);
        ackBy = (TextView)findViewById(R.id.detail_ack_by);
        fixBy = (TextView)findViewById(R.id.detail_fix_by);
        processingAt = (TextView)findViewById(R.id.detail_processing_at);
        desc = (TextView)findViewById(R.id.detail_desc);
        layout = (LinearLayout)findViewById(R.id.issue_detail_layout);

        Intent i = getIntent();
        issueId = i.getIntExtra("issueId",0);

        db = DatabaseManager.getInstance().getDatabase();
        IssueService iService = new IssueService(db);
        ProblemService pService = new ProblemService(db);
        DeptService dService = new DeptService(db);
        SectionService sService = new SectionService(db);
        UserService uService = new UserService(db);

        df = new SimpleDateFormat("hh:mm aa");
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        Cursor c = iService.getIssue(issueId);
        if(c.moveToFirst()){
            int lineNo = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_LINE));
            int probId = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_PROBID));
            int processAt = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_PROCESSING_AT));
            ackby = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_ACK_BY));
            fixby = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_FIX_BY));
            int raiseby = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_RAISED_BY));
            raiseTime = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_RAISED_AT));
            ackTime = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_ACK_AT));
            solveTime = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_FIX_AT));

            problem.setText(pService.getProblemName(probId));
            dept.setText(dService.getDeptName(c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_DEPTID))));
            section.setText(sService.getSectionName(c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_SECID))));
            line.setText("Line " + lineNo);
            opNo.setText(c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_OPERATOR_NO)));
            raisedBy.setText(uService.getUserName(raiseby));
            try {
                raisedAt.setText(df.format(sdf.parse(raiseTime)));
                if(ackTime != null) {
                    ackAt.setText(df.format(sdf.parse(ackTime)));
                }
                if(solveTime != null){
                    fixAt.setText(df.format(sdf.parse(solveTime)));
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            if(ackby != 0) {
                ackBy.setText(uService.getUserName(ackby));
            }

            if(fixby != 0) {
                fixBy.setText(uService.getUserName(fixby));
            }
            if(processAt != 4){
                processingAt.setText(new StringBuilder("Level ").append(String.valueOf(processAt)).append(" user notified.").toString());
            }else{
                processingAt.setText("Fixed");
            }
            desc.setText(c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_DESCRIPTION)));
            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            //Add Acknowledgement Button dynamically///////
            ////////////////////////////////////////////////////////////////////////////////////////////////
            int desgnId = sharedPref.getInt(Constants.DESGN_ID,0);
            userLevel = sharedPref.getInt(Constants.LEVEL,-1);
            DesgnService desgnService = new DesgnService(db);
            Boolean isConcernedUser = desgnService.isConcernedDesgn(desgnId,lineNo,probId);

            if(userLevel == 0){
                if(solveTime == null){
                    if(ackTime == null){
                        ackButton = createButton("ACKNOWLEDGE");
                        ackButton.setBackgroundColor(ContextCompat.getColor(context,R.color.blue));
                        layout.addView(ackButton);
                    }else{
                        fixButton = createButton("FIX");
                        fixButton.setBackgroundColor(ContextCompat.getColor(context,R.color.limeGreen));
                        layout.addView(fixButton);
                    }
                }
            }

            if(isConcernedUser){
                Log.i(TAG,"User is Concerned for problem");
                ackButton = createButton("ACKNOWLEDGE");
                seekHelpBtn = createButton("SEEK HELP");
                ackButton.setBackgroundColor(ContextCompat.getColor(context,R.color.blue));
                seekHelpBtn.setBackgroundColor(ContextCompat.getColor(context,R.color.yellow));

                if(userLevel == 1){
                    Log.i(TAG,"Level 1 : show acknowledge Button");
                    int seekHelp = iService.getSeekHelp(issueId);

                    if(ackTime == null){
                        layout.addView(ackButton);
                    }
                    else if(solveTime == null && seekHelp == 0){
                        layout.addView(seekHelpBtn);
                    }
                }
                if(userLevel == 2){
                    addAckButtonL2();
                }

            }
            //handling onClick Event
            if(ackButton != null) {
                ackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        acknowledge();
                    }
                });
            }
            if(fixButton != null){
                fixButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fix();
                    }
                });
            }
            if(seekHelpBtn != null){
                seekHelpBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seekHelp();
                    }
                });
            }
        }

    }

    private Button createButton(String text){
        Button button = new Button(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );
        button.setLayoutParams(params);
        button.setText(text);
        return button;
    }

    private void addAckButtonL2(){
        Log.i(TAG,"addAckButtonL2() : called for adding acknowledgement button for Level 2 if needed ");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/res/current_time";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"Current time : " + response);
                        if(pDialog != null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        try{
                            long baseTime = sdf.parse(raiseTime).getTime();
                            long interval = 1000*60*sharedPref.getInt(Constants.TIME_ACK,0);
                            long timeNow = Long.parseLong(response);

                            IssueService iService = new IssueService(db);
                            int seekHelp = iService.getSeekHelp(issueId);
                            //Case:1 Should have the acknowledgment button only if level 1 did not acknowledge in time_ack
                            if(timeNow > (baseTime + interval)){
                                if(ackTime == null) {
                                    layout.addView(ackButton);
                                }
                            }
                            if(ackTime != null && solveTime == null) {
                                //if level has acknowledged and seeked help or user himself has acknoledged
                                if (seekHelp == 1 || ackby == userId) {
                                    layout.addView(seekHelpBtn);
                                }
                                else if(seekHelp != 2){
                                    baseTime = sdf.parse(ackTime).getTime();
                                    interval = 1000*60*sharedPref.getInt(Constants.TIME_LEVEL1,0);
                                    if(timeNow > (baseTime + interval)){
                                        layout.addView(seekHelpBtn);
                                    }
                                }
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG,"Unable to get Time" + error.toString());
                        if(pDialog != null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        Toast.makeText(context,"Slow Internet Connection.",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(request);
    }

    private void acknowledge(){
        Log.i(TAG,"acknowledge()");

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/issue/ack";

        Map<String,String> params = new HashMap<>();
        params.put("authToken",authToken);
        params.put("issueId",String.valueOf(issueId));
        params.put("ackBy",String.valueOf(userId));

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"acknowledge Response: " + response);
                if(pDialog != null && pDialog.isShowing()){
                    pDialog.dismiss();
                }
                if(response.contains("success")){
                    Toast.makeText(context,"Acknowledged Successfully.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if(response.contains("closed")){
                    Toast.makeText(context,"Factory Closed.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if(response.contains("not opened")){
                    Toast.makeText(context,"Factory not opened yet.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    //Login info changed. Re-Login
                    Toast.makeText(context,"Login credential changed, Login Again",Toast.LENGTH_SHORT).show();
                    sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
                    Intent i = new Intent(context,LoginActivity.class);
                    startActivity(i);
                }
            }
        };

        RequestString request = new RequestString(url,params,listener,errorListener);
        request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().getRequestQueue().add(request);
    }

    private void fix(){
        Log.i(TAG,"fix()");

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/issue/fix";

        Map<String,String> params = new HashMap<>();
        params.put("authToken",authToken);
        params.put("issueId",String.valueOf(issueId));
        params.put("fixBy",String.valueOf(userId));

        Response.Listener<String> listener =  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"fix Response: " + response);
                if(pDialog != null && pDialog.isShowing()){
                    pDialog.dismiss();
                }
                if(response.contains("success")){
                    Toast.makeText(context,"Issue Fixed Successfully.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if(response.contains("closed")){
                    Toast.makeText(context,"Factory Closed.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if(response.contains("not opened")){
                    Toast.makeText(context,"Factory not opened yet.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    //Login info changed. Re-Login
                    Toast.makeText(context,"Login Credential changed, login Again!",Toast.LENGTH_SHORT).show();
                    sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
                    Intent i = new Intent(context,LoginActivity.class);
                    startActivity(i);
                }
            }
        };
        RequestString request = new RequestString(url,params,listener,errorListener);
        request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().getRequestQueue().add(request);
    }

    private void seekHelp(){
        Log.i(TAG,"seekHelp()");

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/issue/seek_help";
        Map<String,String> params = new HashMap<>();
        params.put("authToken",authToken);
        params.put("issueId",String.valueOf(issueId));
        params.put("level",String.valueOf(userLevel));

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG,"acknowledge Response: " + response);
                if(pDialog != null && pDialog.isShowing()){
                    pDialog.dismiss();
                }
                if(response.contains("success")){
                    Toast.makeText(context,"Help Sought Successfully.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if(response.contains("closed")){
                    Toast.makeText(context,"Factory Closed.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else if(response.contains("not opened")){
                    Toast.makeText(context,"Factory not opened yet.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    //Login info changed. Re-Login
                    Toast.makeText(context,"Login Credential changed, login Again!",Toast.LENGTH_SHORT).show();
                    sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
                    Intent i = new Intent(context,LoginActivity.class);
                    startActivity(i);

                }
            }
        };

        RequestString request = new RequestString(url,params,listener,errorListener);
        request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().getRequestQueue().add(request);
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG,"finish()");
    }
}
