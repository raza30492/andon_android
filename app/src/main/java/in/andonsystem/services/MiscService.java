package in.andonsystem.services;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Administrator on 02-06-2016.
 */
public class MiscService {

    public static final String TIME_SERVER = "time-a.nist.gov";

    public static Boolean isConnectedToInternet(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        Boolean isConnected = (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        return isConnected;
    }

    public static int convertDpToPixel(float dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
