package in.andonsystem;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import in.andonsystem.adapter.AdapterContact;
import in.andonsystem.database.DatabaseManager;
import in.andonsystem.database.DatabaseSchema;
import in.andonsystem.services.DesgnService;
import in.andonsystem.view.DividerItemDecoration;

import in.andonsystem.R;
import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private final String TAG = ContactActivity.class.getSimpleName();

    private Spinner desgnId;

    private LinearLayout layout;
    private RecyclerView recyclerView;
    private TextView textView;
    private ProgressDialog pDialog;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(ContactActivity.this, "544df31b");

        setContentView(R.layout.activity_contact);
        Log.i(TAG,"onCreate()");
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //View Mapping
        desgnId = (Spinner) findViewById(R.id.contact_desgn_id);
        layout = (LinearLayout) findViewById(R.id.contact_layout);

        SQLiteDatabase db = DatabaseManager.getInstance().getDatabase();
        DesgnService desgnService = new DesgnService(db);
        Cursor cursor = desgnService.getDesgns();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context,
                R.layout.spinner_list_item,
                cursor,
                new String[]{DatabaseSchema.TableDesgn.COLUMN_NAME,DatabaseSchema.TableDesgn._ID},
                new int[]{R.id.spinner_item,R.id.id},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        desgnId.setAdapter(adapter);

        recyclerView = new RecyclerView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 30;
        recyclerView.setLayoutParams(params);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,R.drawable.divider));

        textView = new TextView(context);
        textView.setText("No user found for this designation");
        textView.setTextColor(Color.parseColor("#FF0000"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);


        desgnId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(desgnId == null || view == null){
                    return;
                }
                String desgn = ((TextView)view.findViewById(R.id.id)).getText().toString();
                showContacts(Integer.parseInt(desgn));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    public void showContacts(int desgnId){
        Log.i(TAG,"showContacts()");
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.show();

        String url = "http://andonsystem.in/restapi/contacts?desgnId=" + desgnId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG,"Contacts Response : " + response.toString());
                        if(pDialog != null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }

                        List<String> list = new ArrayList<>();
                        try {
                            JSONObject contact;
                            for (int i = 0; i < response.length(); i++) {
                                contact = response.getJSONObject(i);
                                list.add(contact.getString("name") + "\n +91 " + contact.getString("mobile"));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if(response.length() > 0){
                            layout.removeView(recyclerView);
                            layout.removeView(textView);
                            layout.addView(recyclerView,1);

                            AdapterContact adapter = new AdapterContact(context,list);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        }
                        if(response.length() == 0){
                            layout.removeView(recyclerView);
                            layout.removeView(textView);
                            layout.addView(textView,1);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(pDialog != null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        Log.i(TAG,error.toString());
                        Toast.makeText(context,"check your internet connection",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        queue.add(request);
    }

}
