package in.andonsystem.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by Md Zahid Raza on 29-02-2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context){
        super(context, DatabaseSchema.DATABASE_NAME,null, DatabaseSchema.DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("Database","onCreate() called. Creating database");
        db.execSQL(DatabaseSchema.TableDepartment.CREATE_TABLE);
        db.execSQL(DatabaseSchema.TableSection.CREATE_TABLE);
        db.execSQL(DatabaseSchema.TableProblem.CREATE_TABLE);
        db.execSQL(DatabaseSchema.TableIssue.CREATE_TABLE);
        db.execSQL(DatabaseSchema.TableUser.CREATE_TABLE);
        db.execSQL(DatabaseSchema.TableDesgn.CREATE_TABLE);
        db.execSQL(DatabaseSchema.TableDesgnLine.CREATE_TABLE);
        db.execSQL(DatabaseSchema.TableDesgnProblem.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("Database","onUpgrade() called. Upgrading database");
        if( oldVersion != newVersion){
            db.execSQL(DatabaseSchema.TableDepartment.DROP_TABLE);
            db.execSQL(DatabaseSchema.TableSection.DROP_TABLE);
            db.execSQL(DatabaseSchema.TableProblem.DROP_TABLE);
            db.execSQL(DatabaseSchema.TableIssue.DROP_TABLE);
            db.execSQL(DatabaseSchema.TableUser.DROP_TABLE);
            db.execSQL(DatabaseSchema.TableDesgn.DROP_TABLE);
            db.execSQL(DatabaseSchema.TableDesgnLine.DROP_TABLE);
            db.execSQL(DatabaseSchema.TableDesgnProblem.DROP_TABLE);
            onCreate(db);
        }
    }
}
