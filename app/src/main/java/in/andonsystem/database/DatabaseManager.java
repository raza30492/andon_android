package in.andonsystem.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Md Zahid Raza on 29-02-2016.
 */
//Singleton Class for managing database and datbaseHelper class
//This class is responsible for delivering SQLiteDatabse instance and closing it

public class DatabaseManager {

    private int dbCounter;

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    //To avoid direct instaniation
    private DatabaseManager(){}

    public static synchronized void initializeInstance(SQLiteOpenHelper helper){
        if(instance == null){
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance(){
        if(instance == null){
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() + "is not initialized, call initializeInstance() method first.");
        }
        return instance;
    }

    public synchronized SQLiteDatabase getDatabase(){
        dbCounter++;
        if(dbCounter == 1){
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase(){
        dbCounter--;
        if(dbCounter == 0){
            mDatabase.close();
        }
    }




}
