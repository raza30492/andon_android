package in.andonsystem.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by Administrator on 13-07-2016.
 */
public class RequestString extends StringRequest {
    private Map<String,String> params;
    public RequestString(String url, Map<String,String> params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST,url, listener, errorListener);
        this.params = params;
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
