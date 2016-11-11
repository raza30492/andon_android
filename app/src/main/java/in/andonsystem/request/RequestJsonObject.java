package in.andonsystem.request;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 13-07-2016.
 */
public class RequestJsonObject extends JsonObjectRequest{
    private Map<String,String> params;

    public RequestJsonObject(int method, String url, Map<String,String> params, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.params = params;
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
