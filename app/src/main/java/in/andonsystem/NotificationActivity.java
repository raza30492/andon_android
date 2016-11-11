package in.andonsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import in.andonsystem.adapter.AdapterNotification;
import in.andonsystem.database.DatabaseManager;
import in.andonsystem.database.DatabaseSchema;
import in.andonsystem.model.Notification;
import in.andonsystem.services.Constants;
import in.andonsystem.services.DeptService;
import in.andonsystem.services.DesgnService;
import in.andonsystem.services.IssueService;
import in.andonsystem.services.ProblemService;
import in.andonsystem.services.UserService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.TreeSet;

public class NotificationActivity extends AppCompatActivity {

    private final String TAG = NotificationActivity.class.getSimpleName();
    private Context context;
    private SQLiteDatabase db;
    private SharedPreferences sharedPref;

    private long currentTime;
    private RecyclerView recyclerView;
    private TextView textView;
    private RelativeLayout layout;
    private AdapterNotification adapter;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(NotificationActivity.this, "544df31b");

        setContentView(R.layout.activity_notification);
        Log.i(TAG,"onCreate()");
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        //View mapping
        progress = (ProgressBar)findViewById(R.id.loading_progress);
        layout = (RelativeLayout) findViewById(R.id.nfn_layout);
        recyclerView = new RecyclerView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        recyclerView.setLayoutParams(params);
        //recyclerView.addItemDecoration(new DividerItemDecoration(context,R.drawable.divider));
        textView = new TextView(context);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT
        );
        params1.addRule(RelativeLayout.BELOW,R.id.home_filter);
        params1.topMargin = 50;
        textView.setLayoutParams(params1);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(Color.parseColor("#00FF00"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        textView.setText("No Notification available.");

        //variable Initialization
        db = DatabaseManager.getInstance().getDatabase();
        sharedPref = getSharedPreferences(Constants.PREF_FILE_NAME,0);

        progress.setVisibility(View.VISIBLE);
        String url = "http://andonsystem.in/restapi/res/current_time";
        Log.i(TAG,"get current time url :" + url);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"Current time Response : " + response);
                        Log.i(TAG,"Current time : " + response);
                        progress.setVisibility(View.GONE);
                        currentTime = Long.parseLong(response);
                        showNotifications();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG,"Unable to get current Time" + error.toString());
                        progress.setVisibility(View.GONE);
                        Toast.makeText(context,"Check your Internet Connection.",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        queue.add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void showNotifications(){
        Log.i(TAG,"showIssues()");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        IssueService iService = new IssueService(db);
        DesgnService desgnService = new DesgnService(db);
        DeptService dService = new DeptService(db);
        ProblemService pService = new ProblemService(db);
        UserService uService = new UserService(db);

        TreeSet<Notification> issues = new TreeSet<>();

        int desgnId = sharedPref.getInt(Constants.DESGN_ID,0);
        int userId = sharedPref.getInt(Constants.USERID,0);
        int level = sharedPref.getInt(Constants.LEVEL,-1);
        Cursor c;
        if(level == 0){
            Log.i(TAG,"Level 0 , User id : " + userId);
            c = iService.getIssues(userId);
        }else{
            String lines = desgnService.getUserLines(desgnId);
            String problems = desgnService.getUserProblems(desgnId);
            Log.i(TAG,"User responsible for  lines " + lines + " and problems " + problems);
            c = iService.getIssues(lines,problems);
        }

        if(c.getCount() > 0){
            long timeAt;
            int issueId,ackBy;
            String deptName,probName,raisedAt,raisedBy,ackAt,fixAt,message;
            c.moveToFirst();
            do{
                issueId = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue._ID));
                deptName = dService.getDeptName(c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_DEPTID)));
                probName = pService.getProblemName(c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_PROBID)));

                raisedAt = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_RAISED_AT));
                ackAt = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_ACK_AT));
                fixAt = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_FIX_AT));

                raisedBy = uService.getUserName(c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_RAISED_BY)));
                ackBy = c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_ACK_BY));

                try {

                    if (level == 0) {
                        if (ackAt != null) {
                            message = uService.getUserName(ackBy) + " responded to " + probName + " in department " + deptName + ".";
                            timeAt = currentTime - sdf.parse(ackAt).getTime();
                            issues.add(new Notification(issueId, message, timeAt, 1));
                        }
                    } else {

                        if (fixAt != null) {
                            message = probName + " in department " + deptName + " is resolved.";
                            timeAt = currentTime - sdf.parse(fixAt).getTime();
                            issues.add(new Notification(issueId, message, timeAt, 2));
                        }
                        else if (ackAt != null) {
                            message = probName + " in department " + deptName + " addressed by " + (userId == ackBy ? "you" : uService.getUserName(ackBy) )  + ".";
                            timeAt = currentTime - sdf.parse(ackAt).getTime();
                            issues.add(new Notification(issueId, message, timeAt, 1));
                        }else{
                            message = probName + " in department " + deptName + " raised by " + raisedBy + ".";
                            timeAt = currentTime - sdf.parse(raisedAt).getTime();
                            issues.add(new Notification(issueId, message, timeAt, 0));
                        }
                    }
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }while (c.moveToNext());

        }
        if(c.getCount() > 0 && issues.size() > 0){
            Log.i(TAG,"Notifications available for user");
            layout.addView(recyclerView);
            adapter = new AdapterNotification(context,issues);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }else{
            Log.i(TAG,"No Notifications available for user");
            layout.addView(textView);
        }
    }
}
