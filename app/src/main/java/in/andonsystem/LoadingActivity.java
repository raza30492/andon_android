package in.andonsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import in.andonsystem.database.DatabaseManager;
import in.andonsystem.model.Issue;
import in.andonsystem.services.Constants;
import in.andonsystem.services.DeptService;
import in.andonsystem.services.DesgnService;
import in.andonsystem.services.IssueService;
import in.andonsystem.services.MiscService;
import in.andonsystem.services.ProblemService;
import in.andonsystem.services.SectionService;
import in.andonsystem.services.UserService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LoadingActivity extends AppCompatActivity {

    private String TAG = LoadingActivity.class.getSimpleName();
    private SharedPreferences sharedPref;
    private SQLiteDatabase db;
    private RequestQueue queue;
    private Boolean isConnected;
    private AlertDialog dialog;
    private Boolean flag;  //To indicate whether to initialize or re-initialize. value: false - init directly called, true : init called due to relaunch
    private long launchTime;

    private long currentTime;
    private Context context;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(LoadingActivity.this, "544df31b");
        setContentView(R.layout.activity_loading);
        Log.d(TAG,"onCreate()");
        App.activity1 = this;
        context = this;

        progress = (ProgressBar)findViewById(R.id.loading_progress);

        //Global Initialization
        queue = AppController.getInstance().getRequestQueue();
        db = DatabaseManager.getInstance().getDatabase();
        sharedPref = getSharedPreferences(Constants.PREF_FILE_NAME,0);
        flag = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart()");

        //Handle Internet Connection
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet");
        builder.setMessage("No Internet Connection Available.Do you want to try again?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onStart();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        dialog = builder.create();

        isConnected = MiscService.isConnectedToInternet(this);
        if(!isConnected) {
            dialog.show();
        }
        if(isConnected){
            Boolean firstLaunch = sharedPref.getBoolean(Constants.FIRST_LAUNCH,true);

            progress.setVisibility(View.VISIBLE);

            //If Application is launched for first time
            if (firstLaunch) {
                init();
            } else {   //Else if WebApplication was Relaunched later

                long appLaunch = sharedPref.getLong(Constants.WEBAPP_LAUNCH, 0L);
                String url = "http://andonsystem.in/restapi/misc/relaunch?time=" + appLaunch + "&version=" + getString(R.string.version2);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "Relauch Response : " + response.toString());
                            flag = true;
                            try {
                                String relaunched = response.getString("relaunched");
                                String updateApp = response.getString("updateApp");
                                if(updateApp.equals("YES")){
                                    AlertDialog.Builder builder3 = new AlertDialog.Builder(context);
                                    builder3.setTitle("Update Available");
                                    builder3.setMessage("A new version of application is available.Please update for app to work properly.");
                                    builder3.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://andonsystem.in/download.jsp"));
                                            startActivity(intent);
                                        }
                                    });
                                    builder3.setNegativeButton("LATER", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            goToLogin();
                                        }
                                    });
                                    builder3.create().show();
                                }
                                if(relaunched.equals("YES")){
                                    Log.d(TAG, "WebApp Relaunched. Re-Initializing database.");
                                    launchTime = response.getLong("launch_time");

                                    //Delete all the entries of tables and re-initialize
                                    new SectionService(db).deleteSections();
                                    new DeptService(db).deleteDepts();
                                    new ProblemService(db).deleteProblems();
                                    new IssueService(db).deleteIssues();
                                    new UserService(db).deleteUsers();
                                    new DesgnService(db).deleteDesgns();

                                    init();
                                }else{
                                    //Delete Previous day Issues
                                    currentTime = response.getLong("current_time");
                                    IssueService iService = new IssueService(db);
                                    int count = iService.deleteOldIssues(currentTime);
                                    Log.i(TAG,"No. of Issues deleted :" + count);
                                    progress.setVisibility(View.GONE);
                                    goToLogin();
                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, error.toString());
                            Toast.makeText(context,"Slow Internet Connection",Toast.LENGTH_SHORT).show();
                            progress.setVisibility(View.GONE);
                            goToLogin();
                        }
                    }
                );

                Log.i(TAG, "Relaunch url :" + url);
                queue.add(request);

            }
        }
    }

    private void init(){
        String url =  "http://andonsystem.in/restapi/res/init";
        Log.i(TAG,"init url : " + url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG,"Initialization Response: " + response.toString());
                    processResponse(response);
                    if(flag){
                        sharedPref.edit().putLong(Constants.WEBAPP_LAUNCH,launchTime).commit();
                    }
                    progress.setVisibility(View.GONE);
                    goToLogin();

                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, error.toString());
                    progress.setVisibility(View.GONE);
                    //Handle slow Internet Connection
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                    builder2.setTitle("Slow Internet");
                    builder2.setMessage("Internet Connection is too slow.Do you want to try again?");
                    builder2.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            onStart();
                        }
                    });
                    builder2.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder2.create().show();
                }
            });

        queue.add(req);

    }

    public void  goToLogin(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void processResponse(JSONObject response){

        try {
            long appLaunch = response.getLong("launchTime");
            long issueSync = response.getLong("issueSync");
            int lines = response.getInt("lines");
            int timeAck = response.getInt("timeAck");
            int timeLevel1 = response.getInt("timeLevel1");
            int timeLevel2 = response.getInt("timeLevel2");

            JSONArray sections = response.getJSONArray("sections");
            JSONArray depts = response.getJSONArray("departments");
            JSONArray problems = response.getJSONArray("problems");
            JSONArray issues = response.getJSONArray("issues");
            JSONArray users = response.getJSONArray("users");
            JSONArray desgns = response.getJSONArray("desgns");
            JSONArray desgnLines = response.getJSONArray("desgnLine");
            JSONArray desgnProblems = response.getJSONArray("desgnProblem");
//////////////////////////////////////////////////////////////////////////
            Log.d(TAG,"Saving sections in database");
            JSONObject section;
            SectionService sService = new SectionService(db);
            for(int i = 0; i < sections.length() ; i++){
                section = sections.getJSONObject(i);
                sService.insertSection(section.getInt("id"),section.getString("name"));
            }
            sService.insertSection(0,"Select Section");
///////////////////////////////////////////////////////////////////////////////
            Log.d(TAG,"Saving departments in database");
            JSONObject dept;
            DeptService dService = new DeptService(db);
            for(int i = 0; i < depts.length() ; i++){
                dept = depts.getJSONObject(i);
                dService.insertDept(dept.getInt("id"),dept.getString("name"));
            }
            dService.insertDept(0,"Select department");
/////////////////////////////////////////////////////////////////////////////////////
            Log.d(TAG,"Saving Problems in database");
            JSONObject problem;
            ProblemService pService = new ProblemService(db);
            for(int i = 0; i < problems.length() ; i++){
                problem = problems.getJSONObject(i);
                pService.insertProblem(problem.getInt("probId"),problem.getInt("deptId"),problem.getString("name"));
            }
            pService.insertProblem(0,0,"Select Issue");
//////////////////////////////////////////////////////////////////////////////////////
            JSONObject issue;
            int id,line,secId,deptId,probId,status,raisedBy,ackBy,fixBy,processingAt,seekHelp;
            long raisedAt,ackAt,fixAt;
            String critical,operatorNo,desc;
            Issue issueObj;

            IssueService iService = new IssueService(db);
            Log.i(TAG,"Inserting Issues in Database");
            for(int i = 0; i < issues.length() ; i++){
                issue = issues.getJSONObject(i);
                id = issue.getInt("id");
                line = issue.getInt("line");
                secId = issue.getInt("secId");
                deptId = issue.getInt("deptId");
                probId = issue.getInt("probId");
                critical = issue.getString("critical");
                operatorNo = issue.getString("operatorNo");
                desc = issue.getString("desc");
                raisedAt = issue.getLong("raisedAt");
                ackAt = issue.getLong("ackAt");
                fixAt = issue.getLong("fixAt");
                raisedBy = issue.getInt("raisedBy");
                ackBy = issue.getInt("ackBy");
                fixBy = issue.getInt("fixBy");
                processingAt = issue.getInt("processingAt");
                status = issue.getInt("status");
                seekHelp = issue.getInt("seekHelp");

                issueObj = new Issue(id,line,secId,deptId,probId,critical,operatorNo,desc,raisedAt,ackAt,fixAt,raisedBy,ackBy,fixBy,processingAt,status,seekHelp);
                iService.insertIssue(issueObj);
            }
///////////////////////////////////////////////////////////////////////////////////////////////////////
            int userId,desgnId;
            String username,mobile;
            JSONObject user;
            UserService uService = new UserService(db);
            Log.i(TAG,"Saving Users in Database");
            for( int i = 0; i < users.length() ; i++){
                user = users.getJSONObject(i);
                userId = user.getInt("userId");
                username = user.getString("username");
                desgnId = user.getInt("desgnId");
                mobile = user.getString("mobile");

                uService.saveUser(userId,username,desgnId,mobile);
            }
///////////////////////////////////////////////////////////////////////////////////////////////////////////
            DesgnService desgnService = new DesgnService(db);
            Log.i(TAG,"Saving Designation in Database");
            JSONObject desgn,desgnLine,desgnProblem;
            for(int i = 0; i < desgns.length() ; i++){
                desgn = desgns.getJSONObject(i);
                desgnService.saveDesgn(desgn.getInt("desgnId"),desgn.getString("name"));
            }
            desgnService.saveDesgn(0,"All Users");
////////////////////////////////////////////////////////////////////////////////////////////////
            Log.i(TAG,"Saving Designation Line Mapping in Database");
            for(int i = 0; i < desgnLines.length(); i++){
                desgnLine = desgnLines.getJSONObject(i);
                desgnService.saveDesgnLine(desgnLine.getInt("key"),desgnLine.getInt("value"));
            }
////////////////////////////////////////////////////////////////////////////////////////////////
            Log.i(TAG,"Saving Designation Problem Mapping in Database");
            for(int i = 0; i < desgnProblems.length(); i++){
                desgnProblem = desgnProblems.getJSONObject(i);
                desgnService.saveDesgnProblem(desgnProblem.getInt("key"),desgnProblem.getInt("value"));
            }
/////////////////////////////////////////////////////////////////////////////////////////////////
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.FIRST_LAUNCH,false);
            editor.putLong(Constants.WEBAPP_LAUNCH,appLaunch);
            editor.putLong(Constants.ISSUE_SYNC,issueSync);
            editor.putInt(Constants.LINES,lines);
            editor.putInt(Constants.TIME_ACK,timeAck);
            editor.putInt(Constants.TIME_LEVEL1,timeLevel1);
            editor.putInt(Constants.TIME_LEVEL2,timeLevel2);

            editor.commit();

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        App.activity1 = null;
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG,"finish()");
    }
}




