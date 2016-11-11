package in.andonsystem;

import android.app.Activity;

/**
 * Created by Md Zahid Raza on 20/06/2016.
 */
public class App {

    public static Activity activity1;   //LoadingActivity
    public static Activity activity2;   //LoginActivity
    public static Activity activity3;   //HomeActivity
    public static Activity activity4;

    public static void close() {
        if( App.activity4 != null){
            App.activity4.finish();
        }
        if (App.activity3 != null) {
            App.activity3.finish();
        }
        if (App.activity2 != null) {
            App.activity2.finish();
        }
        if (App.activity1 != null) {
            App.activity1.finish();
        }

    }
}
