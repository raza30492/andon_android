package in.andonsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import in.andonsystem.database.DatabaseManager;
import in.andonsystem.database.DatabaseSchema;
import in.andonsystem.services.Constants;
import in.andonsystem.services.DeptService;
import in.andonsystem.services.ProblemService;
import in.andonsystem.services.SectionService;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import org.json.JSONObject;


public class RaiseIssueActivity extends AppCompatActivity {


    private final String TAG = RaiseIssueActivity.class.getSimpleName();

    private LinearLayout container;
    private Spinner lines;
    private Spinner sections;
    private Spinner depts;
    private Spinner problems;
    private EditText operatorNo;
    private EditText issueDesc;
    private RadioGroup radioGroup;
    private Button raiseBtn;

    private SharedPreferences sharedPref;
    private ProgressDialog pDialog;
    private Context context;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(RaiseIssueActivity.this, "544df31b");
        setContentView(R.layout.activity_raise_issue);
        Log.i(TAG,"onCreate()");
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //View mapping
        container = (LinearLayout)findViewById(R.id.raise_container);
        lines = (Spinner)findViewById(R.id.line_dropdown);
        sections = (Spinner)findViewById(R.id.section_dropdown);
        depts = (Spinner)findViewById(R.id.dept_dropdown);
        problems = (Spinner)findViewById(R.id.problem_dropdown);
        operatorNo = (EditText)findViewById(R.id.operator_no);
        issueDesc = (EditText)findViewById(R.id.issue_desc);
        raiseBtn = (Button)findViewById(R.id.raise_btn);
        //Initialization
        sharedPref = getSharedPreferences(Constants.PREF_FILE_NAME,0);
        db = DatabaseManager.getInstance().getDatabase();
        SectionService sService = new SectionService(db);
        DeptService dService = new DeptService(db);


        int noOfLines = sharedPref.getInt(Constants.LINES,0);
        String[] lineArray = new String[noOfLines+1];
        lineArray[0] = "Select Line";
        for(int i = 1; i < lineArray.length; i++){
            lineArray[i] = "Line " + (i);
        }

        ArrayAdapter<String> lineAdapter = new ArrayAdapter<>(this,R.layout.spinner_list_item,R.id.spinner_item,lineArray);
        lineAdapter.setDropDownViewResource(R.layout.spinner_list_item);
        lines.setAdapter(lineAdapter);

        Cursor secCursor = sService.getSections();
        SimpleCursorAdapter sectionAdapter = new SimpleCursorAdapter(
                this,
                R.layout.spinner_list_item,
                secCursor,
                new String[]{DatabaseSchema.TableSection.COLUMN_NAME,DatabaseSchema.TableSection._ID},
                new int[]{R.id.spinner_item,R.id.id},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        sections.setAdapter(sectionAdapter);

        Cursor deptCursor = dService.getDepts();
        SimpleCursorAdapter deptAdapter = new SimpleCursorAdapter(
                this,
                R.layout.spinner_list_item,
                deptCursor,
                new String[]{DatabaseSchema.TableSection.COLUMN_NAME,DatabaseSchema.TableDepartment._ID},
                new int[]{R.id.spinner_item,R.id.id},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        depts.setAdapter(deptAdapter);
        depts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateProblem();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
/*
        issueDesc.setImeActionLabel("Raise",KeyEvent.KEYCODE_ENTER);
        issueDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    raiseBtn.performClick();
                    return true;
                }
                return false;
            }
        });
*/

    }

    public void raiseIssue(View v){
        Log.i(TAG,"raiseIssue()");

        String lineStr = ((TextView)lines.findViewById(R.id.spinner_item)).getText().toString();
        String secStr = ((TextView)sections.findViewById(R.id.id)).getText().toString();
        String deptStr = ((TextView)depts.findViewById(R.id.id)).getText().toString();
        String probStr = ((TextView)problems.findViewById(R.id.id)).getText().toString();
        String opNo = operatorNo.getText().toString();
        String desc = issueDesc.getText().toString();

        if(lineStr.contains("Select")){
            Snackbar.make(container,"Select Line first",Snackbar.LENGTH_SHORT).show();
        }else if(secStr.equals("0")){
            Snackbar.make(container,"Select Section first",Snackbar.LENGTH_SHORT).show();
        }else if(deptStr.equals("0")){
            Snackbar.make(container,"Select Department first",Snackbar.LENGTH_SHORT).show();
        }else if(probStr.equals("0")){
            Snackbar.make(container,"Select Issue first",Snackbar.LENGTH_SHORT).show();
        }else if(opNo.equals("")){
            Snackbar.make(container,"Enter Operator number first",Snackbar.LENGTH_SHORT).show();
        } else if(desc.equals("")){
            Snackbar.make(container,"Enter Description first",Snackbar.LENGTH_SHORT).show();
        }else {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Please wait...");
            pDialog.show();
            int line = Integer.parseInt(lineStr.split(" ")[1]);
            int secId = Integer.parseInt(secStr);
            int deptId = Integer.parseInt(deptStr);
            final int probId = Integer.parseInt(probStr);

            String critical = "NO";

            if (deptId == 4) {
                Log.i(TAG, "Maintenance Section");
                radioGroup = (RadioGroup) findViewById(R.id.radio_group);
                int id = radioGroup.getCheckedRadioButtonId();
                if (id == R.id.radio_critical) {
                    critical = "YES";
                }
            }
            JSONObject data = new JSONObject();
            try {
                data.put("line", line);
                data.put("secId", secId);
                data.put("deptId", deptId);
                data.put("probId", probId);
                data.put("critical", critical);
                data.put("operatorNo", opNo);
                data.put("desc", desc);
                data.put("raisedBy", sharedPref.getInt(Constants.USERID, 0));

            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Request data : " + data.toString());
            String authToken = sharedPref.getString(Constants.AUTHTOKEN, null);
            if (authToken == null) {
                Intent i = new Intent(context, LoginActivity.class);
                startActivity(i);
            } else {
                String url = "http://andonsystem.in/restapi/issue?authToken=" + authToken;

                JsonRequest<String> request = new JsonRequest<String>(Request.Method.POST, url, data.toString(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i(TAG, "Post Issue Response:" + response);
                                if(pDialog != null & pDialog.isShowing()){
                                    pDialog.dismiss();
                                }
                                if (response.contains("success")) {
                                    Toast.makeText(context,"Issue Raised Successfully.",Toast.LENGTH_SHORT).show();
                                    //Snackbar.make(container,"Issue Raised Successfully.",Snackbar.LENGTH_SHORT).show();
                                    finish();
                                }
                                else if(response.contains("closed")){
                                    Toast.makeText(context,"Factory is Closed.",Toast.LENGTH_SHORT).show();
                                    //Snackbar.make(container,"Factory is Closed.",Snackbar.LENGTH_SHORT).show();
                                    finish();
                                }
                                else if(response.contains("not opened")){
                                    Toast.makeText(context,"Factory not opened yet.",Toast.LENGTH_SHORT).show();
                                    //Snackbar.make(container,"Factory not opened yet.",Snackbar.LENGTH_SHORT).show();
                                    finish();
                                }else{
                                    Toast.makeText(context,"Login credendial changed, Login Again!",Toast.LENGTH_LONG).show();
                                    sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
                                    Intent i = new Intent(context,LoginActivity.class);
                                    startActivity(i);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i(TAG, error.toString());
                                if(pDialog != null & pDialog.isShowing()){
                                    pDialog.dismiss();
                                }
                                Snackbar.make(container,"Slow Internet Connection, Try Again!",Snackbar.LENGTH_SHORT).show();
                            }
                        }) {
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        int statusCode = response.statusCode;
                        if (statusCode == 200) {
                            try {
                                String result = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                                return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));

                            } catch (Exception e) {
                                return Response.error(new ParseError(e));
                            }
                        }
                        return Response.error(new ParseError(response));
                    }
                };

                Log.i(TAG, "Post issue url : " + url);

                RequestQueue queue = AppController.getInstance().getRequestQueue();
                request.setRetryPolicy( new DefaultRetryPolicy(20*1000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            }
        }

    }

    private void updateProblem(){
        ProblemService pService = new ProblemService(db);

        TextView dept = (TextView)depts.findViewById(R.id.id);

        if(dept != null){
            int deptId = Integer.parseInt(dept.getText().toString());
            Cursor probCursor = pService.getProblems(deptId);

            SimpleCursorAdapter probAdapter = new SimpleCursorAdapter(
                    this,
                    R.layout.spinner_list_item,
                    probCursor,
                    new String[]{DatabaseSchema.TableProblem.COLUMN_NAME,DatabaseSchema.TableProblem._ID},
                    new int[]{R.id.spinner_item,R.id.id},
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
            );
            problems.setAdapter(probAdapter);

            TextView deptName = (TextView)depts.findViewById(R.id.spinner_item);

            RadioGroup rg = (RadioGroup)findViewById(R.id.radio_group);
            if(deptName.getText().toString().contains("Maintenance")){

                RadioButton critical = new RadioButton(this);
                critical.setId(R.id.radio_critical);
                critical.setText("Critical");
                critical.setChecked(true);
                rg.addView(critical);

                RadioButton nonCritical = new RadioButton(this);
                nonCritical.setId(R.id.radio_non_critical);
                nonCritical.setText("Non Critical");
                rg.addView(nonCritical);

            }else{
                rg.removeAllViews();
            }
        }

    }

    @Override
    public void finish() {
        super.finish();
    }
}
