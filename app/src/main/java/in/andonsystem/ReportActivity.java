package in.andonsystem;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import in.andonsystem.adapter.AdapterReport;
import in.andonsystem.database.DatabaseManager;
import in.andonsystem.model.Downtime;
import in.andonsystem.services.DeptService;
import in.andonsystem.services.ProblemService;
import in.andonsystem.view.DividerItemDecoration;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener  {

    private final String TAG = ReportActivity.class.getSimpleName();
    private Context context;
    private ProgressBar progress;

    private TextView dateView;
    private TextView message;
    private RecyclerView recyclerView;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(ReportActivity.this, "544df31b");
        setContentView(R.layout.activity_report);
        Log.i(TAG,"onCreate()");
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progress = (ProgressBar)findViewById(R.id.loading_progress);

        container = (LinearLayout)findViewById(R.id.report_container);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        String date = String.format("%02d/%02d/%04d",day,month+1,year);

        dateView = (TextView)findViewById(R.id.date_view);
        dateView.setText(date);

        recyclerView = new RecyclerView(context);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        recyclerView.setLayoutParams(params);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,R.drawable.divider));

        message = new TextView(context);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params2.gravity = Gravity.CENTER_HORIZONTAL;
        message.setText("No Report for selected day found");
        message.setTextColor(ContextCompat.getColor(context,R.color.tomato));

        showReport(date);
    }

    public void selectDate(View view){
        Log.i(TAG,"selectDate()");
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showReport(String date){
        Log.i(TAG,"showReport()");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        progress.setVisibility(View.VISIBLE);

        String url = "http://andonsystem.in/restapi/report?date="+date;
        Log.i(TAG,"Report request url :" + url);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG,"Report Response: "+response.toString());
                        progress.setVisibility(View.GONE);

                        if(response.length() == 0){
                            container.removeView(recyclerView);
                            container.removeView(message);
                            container.addView(message);
                        }else{
                            SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
                            ProblemService pService = new ProblemService(db);
                            DeptService dService = new DeptService(db);

                            List<Downtime> list = new ArrayList<>();

                            try {
                                int probId, deptId, downtime,line;
                                JSONObject jsonObject;

                                for (int i = 0; i < response.length(); i++) {
                                    jsonObject = response.getJSONObject(i);

                                    probId = jsonObject.getInt("probId");
                                    deptId = jsonObject.getInt("deptId");
                                    downtime = jsonObject.getInt("downtime");
                                    line = jsonObject.getInt("line");

                                    list.add(new Downtime(
                                            pService.getProblemName(probId),
                                            dService.getDeptName(deptId),
                                            line,
                                            downtime
                                    ));
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            container.removeView(recyclerView);
                            container.removeView(message);
                            container.addView(recyclerView);
                            AdapterReport adapter = new AdapterReport(context,list);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG,error.toString());
                        progress.setVisibility(View.GONE);
                        Toast.makeText(context,"Check Your Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(request);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(view.isShown()) {
            Log.i(TAG, "onDateSet()");
            String date = String.format("%02d/%02d/%04d", day, month + 1, year);
            dateView.setText(date);
            showReport(date);
        }
    }

    public static  class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), (ReportActivity)getActivity(), year, month, day);
        }
    }

}
