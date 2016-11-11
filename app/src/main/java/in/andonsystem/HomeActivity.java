package in.andonsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import in.andonsystem.adapter.AdapterHome;
import in.andonsystem.database.DatabaseManager;
import in.andonsystem.database.DatabaseSchema;
import in.andonsystem.model.Issue;
import in.andonsystem.model.Problem;
import in.andonsystem.services.Constants;
import in.andonsystem.services.DeptService;
import in.andonsystem.services.IssueService;
import in.andonsystem.services.ProblemService;
import in.andonsystem.services.SectionService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = HomeActivity.class.getSimpleName();

    private Spinner line;
    private Spinner section;
    private Spinner dept;
    private int lineNo;
    private int sectionId;
    private int departmentId;
    private int desgnId;
    private int level;
    private Boolean flagLine,flagSection,flagDept;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private RelativeLayout container;
    private TextView textView;
    private NavigationView navigationView;

    private Boolean rvViewAdded;
    private SQLiteDatabase db;
    private DateFormat df;
    private SharedPreferences sharedPref;
    private Context context;
    private AlertDialog exitDialog;

    private ProblemService pService;
    private IssueService iService;
    private DeptService dService;
    private AdapterHome rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(HomeActivity.this, "544df31b");
        Mint.leaveBreadcrumb("home activity created");
        setContentView(R.layout.activity_home);
        Log.i(TAG,"onCreate()");
        App.activity3 = this;
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //views mapping
        line = (Spinner)findViewById(R.id.home_line);
        section = (Spinner)findViewById(R.id.home_section);
        dept = (Spinner)findViewById(R.id.home_department);
        container = (RelativeLayout)findViewById(R.id.home_container);
        //Create swipe refresh layout
        refreshLayout = new SwipeRefreshLayout(context);
        RelativeLayout.LayoutParams param1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        param1.addRule(RelativeLayout.BELOW,R.id.home_filter);
        refreshLayout.setLayoutParams(param1);
        //create recycler view
        recyclerView = new RecyclerView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        recyclerView.setLayoutParams(params);
        //create text view
        textView = new TextView(context);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT
        );
        params1.addRule(RelativeLayout.BELOW,R.id.home_filter);
        params1.topMargin = 50;
        textView.setLayoutParams(params1);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(ContextCompat.getColor(context,R.color.limeGreen));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        textView.setText("No Open Issues Found.");


        //variable Initialization
        flagLine = false; flagSection = false; flagDept = false;
        sharedPref = getSharedPreferences(Constants.PREF_FILE_NAME,0);
        db = DatabaseManager.getInstance().getDatabase();
        dService = new DeptService(db);
        pService = new ProblemService(db);
        iService = new IssueService(db);
        desgnId = sharedPref.getInt(Constants.DESGN_ID,-1);
        level = sharedPref.getInt(Constants.LEVEL,-1);
        df = new SimpleDateFormat("hh:mm aa");
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        SectionService sService = new SectionService(db);

        //FloatingActionButton
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundColor(Color.parseColor("#0000FF"));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                raiseIssue();
            }
        });
        fab.hide();
        if(level == 0){
            fab.show();
        }

        //DrawerLayout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //NavigationView
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView username = (TextView) header.findViewById(R.id.nav_header_username);
        String user = sharedPref.getString(Constants.USERNAME,null);
        username.setText(user);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,ProfileActivity.class);
                startActivity(i);
            }
        });
        if(level != 0){
            Menu menu = navigationView.getMenu();
            menu.removeItem(R.id.nav_stylechangeover);
        }

        //Filters
        int noOfLines = sharedPref.getInt(Constants.LINES,0);
        String[] lineArray = new String[noOfLines+1];
        lineArray[0] = "Select Line";
        for(int i = 1; i < lineArray.length; i++){
            lineArray[i] = "Line " + i;
        }
        //Line Filter
        ArrayAdapter<String> lineAdapter = new ArrayAdapter<>(this,R.layout.spinner_list_item,R.id.spinner_item,lineArray);
        lineAdapter.setDropDownViewResource(R.layout.spinner_list_item);
        line.setAdapter(lineAdapter);
        //Section Filter
        Cursor secCursor = sService.getSections();
        SimpleCursorAdapter sectionAdapter = new SimpleCursorAdapter(
                this,
                R.layout.spinner_list_item,
                secCursor,
                new String[]{DatabaseSchema.TableSection.COLUMN_NAME,DatabaseSchema.TableSection._ID},
                new int[]{R.id.spinner_item,R.id.id},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        section.setAdapter(sectionAdapter);
        //Department Filter
        Cursor deptCursor = dService.getDepts();
        SimpleCursorAdapter deptAdapter = new SimpleCursorAdapter(
                this,
                R.layout.spinner_list_item,
                deptCursor,
                new String[]{DatabaseSchema.TableSection.COLUMN_NAME,DatabaseSchema.TableDepartment._ID},
                new int[]{R.id.spinner_item,R.id.id},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        dept.setAdapter(deptAdapter);

        line.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG,"onItemSelect() : line");
                if(line == null || view == null){
                    return;
                }
                String lineStr = ((TextView)view.findViewById(R.id.spinner_item)).getText().toString();
                if(lineStr.contains("Select")){
                    lineNo = 0;
                }else{
                    lineNo = Integer.parseInt(lineStr.split(" ")[1]);
                }
                if(flagLine == false){
                    flagLine = true;
                }else {
                    showIssues();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        section.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG,"onItemSelect() : section");
                if(section == null || view == null){
                    return;
                }
                String secIdStr = ((TextView)view.findViewById(R.id.id)).getText().toString();
                sectionId = Integer.parseInt(secIdStr);
                if(flagSection == false){
                    flagSection = true;
                }else {
                    showIssues();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        dept.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG,"onItemSelect() : department");
                if(dept == null || view == null){
                    return;
                }
                String deptIdStr = ((TextView)view.findViewById(R.id.id)).getText().toString();
                departmentId = Integer.parseInt(deptIdStr);
                if(flagDept == false){
                    flagDept = true;
                }else {
                    showIssues();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Swipe Refresh
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncIssues();
            }
        });
        showIssues();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //Quit Application
            App.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        if(desgnId == 43) {     ///SMED Executive
            menu.removeItem(R.id.action_notification);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            syncIssues();
        }
        if(id == R.id.action_notification){
            Intent i = new Intent(context,NotificationActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart()");
        syncIssues();
    }

    @Override
    protected void onDestroy() {
        App.activity3 = null;
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG,"finish()");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_stylechangeover) {
            Intent i = new Intent(context,StyleChangeOverActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_report) {
            Intent i = new Intent(context,ReportActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_contacts) {
            Intent i = new Intent(context,ContactActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_help) {
            Intent i = new Intent(context,HelpActivity.class);
            startActivity(i);
        }else if (id == R.id.nav_about) {
            Intent i = new Intent(context,AboutActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
            finish();
            //Intent i = new Intent(context,LoginActivity.class);
            //startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void raiseIssue(){
        Log.i(TAG,"raiseIssues()");
        Intent i = new Intent(this,RaiseIssueActivity.class);
        startActivity(i);
    }

    private Problem getData(int issueId, int line, int deptId, int probId, String critical, String raiseTime,int downtime, int flag){
        ProblemService pService = new ProblemService(db);
        DeptService dService = new DeptService(db);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        Problem issue = null;
        try{
            issue =  new Problem(
                    issueId,
                    "Line " + line,
                    dService.getDeptName(deptId),
                    pService.getProblemName(probId),
                    critical,
                    df.format(sdf.parse(raiseTime)),
                    downtime,
                    flag
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        return  issue;
    }

    public void showIssues(){
        Log.i(TAG,"showIssues()");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        TreeSet<Problem> issues = new TreeSet<>();
        String raisedAt,ackAt,fixAt;
        int flag,downtime;

        Cursor c = iService.getIssues(lineNo,sectionId,departmentId);

        if(c.getCount() > 0){

            Log.i(TAG,"No. of Issues available to show:" + c.getCount());
            c.moveToFirst();
            do{
                downtime = -1;
                raisedAt = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_RAISED_AT));
                ackAt = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_ACK_AT));
                fixAt = c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_FIX_AT));
                if(fixAt != null){
                    flag = 2;
                    try {
                        downtime = (int) TimeUnit.MILLISECONDS.toMinutes((sdf.parse(fixAt).getTime() - sdf.parse(raisedAt).getTime()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if(ackAt != null){
                    flag = 1;
                }else{
                    flag = 0;
                }

                Problem issue = getData(
                        c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue._ID)),
                        c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_LINE)),
                        c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_DEPTID)),
                        c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_PROBID)),
                        c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_CRITICAL)),
                        c.getString(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_RAISED_AT)),
                        downtime,
                        flag
                );
                issues.add(issue);
            }while (c.moveToNext());

            //Remove both views first if exist
            container.removeView(textView);
            refreshLayout.removeView(recyclerView);
            container.removeView(refreshLayout);

            //Add recyclerView
            //if(refreshLayout != null)
            container.addView(refreshLayout);
            refreshLayout.addView(recyclerView);
            rvViewAdded = true;
            rvAdapter = new AdapterHome(this,issues);
            recyclerView.setAdapter(rvAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        }else{
            //Remove both views first if exist
            container.removeView(textView);
            refreshLayout.removeView(recyclerView);
            container.removeView(refreshLayout);
            rvViewAdded = false;
            //Add textView
            container.addView(textView);
        }
    }

    public void syncIssues(){
        Log.i(TAG,"syncIssues()");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        refreshLayout.setRefreshing(true);

        Long syncTime = sharedPref.getLong(Constants.ISSUE_SYNC,0);

        String url = "http://andonsystem.in/restapi/issues/" + syncTime.toString();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG,"Sync Response: " + response.toString());

                    try{
                        long issueSync = response.getLong("issueSync");

                        JSONArray issues = response.getJSONArray("issues");

                        //Add recycleView if not added
                        if(issues.length() > 0 && !rvViewAdded ){
                            container.removeView(textView);
                            refreshLayout.removeView(recyclerView);
                            container.removeView(refreshLayout);
                            rvViewAdded = true;

                            TreeSet<Problem> issue = new TreeSet<>();
                            rvAdapter = new AdapterHome(context,issue);
                            container.addView(refreshLayout);
                            refreshLayout.addView(recyclerView);
                            recyclerView.setAdapter(rvAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        }

                        //variable Declarations
                        JSONObject issue;
                        int id,line,secId,deptId,probId,status,raisedBy,ackBy,fixBy,processingAt,flag,seekHelp;
                        long raisedAt,ackAt,fixAt;
                        String critical,operatorNo,desc;
                        Issue issueObj;

                        //Saving Issues in Database
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

                            Log.i(TAG,"Inserting Issues in Database");
                            iService.insertIssue(issueObj);


                            if((lineNo == 0 || lineNo == line) && (sectionId == 0 || sectionId == secId) && (departmentId == 0 || departmentId == deptId)) {
                                //adding or modifying in adapter
                                int downtime = -1;
                                if (fixAt != 0) {
                                    flag = 2;
                                    downtime = (int)TimeUnit.MILLISECONDS.toMinutes(fixAt-raisedAt);
                                } else if (ackAt != 0) {
                                    flag = 1;
                                } else {
                                    flag = 0;
                                }

                                String time = df.format(new Date(raisedAt));
                                Problem data = new Problem(
                                        id,
                                        "Line " + line,
                                        dService.getDeptName(deptId),
                                        pService.getProblemName(probId),
                                        critical,
                                        time,
                                        downtime,
                                        flag
                                );

                                if (flag == 0) {
                                    Log.i(TAG, "Adapter : add Issue");
                                    rvAdapter.insert(data);
                                } else {
                                    Log.i(TAG, "Adapter : update Issue");
                                    rvAdapter.update(data);
                                }
                            }
                        }
                        sharedPref.edit().putLong(Constants.ISSUE_SYNC,issueSync).commit();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    refreshLayout.setRefreshing(false);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context,"Unable to Sync. Check your Internet Connection.",Toast.LENGTH_SHORT).show();
                    showIssues();
                    refreshLayout.setRefreshing(false);
                }
            }
        );
        queue.add(req);

    }

}
