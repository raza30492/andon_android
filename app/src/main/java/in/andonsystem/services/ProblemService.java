package in.andonsystem.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.andonsystem.database.DatabaseSchema;

/**
 * Created by Administrator on 01-06-2016.
 */
public class ProblemService {
    private SQLiteDatabase mDatabase;

    public ProblemService(SQLiteDatabase db){
        this.mDatabase = db;
    }

    public long insertProblem(int probId,int deptId,String name){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableProblem._ID,probId);
        values.put(DatabaseSchema.TableProblem.COLUMN_DEPT_ID,deptId);
        values.put(DatabaseSchema.TableProblem.COLUMN_NAME,name);

        long result = mDatabase.insert(DatabaseSchema.TableProblem.TABLE_NAME,null,values);
        return result;
    }

    public Cursor getProblems(int deptId){
        String[] projection = {
                DatabaseSchema.TableProblem._ID,
                DatabaseSchema.TableProblem.COLUMN_NAME
        };

        String selection = DatabaseSchema.TableProblem.COLUMN_DEPT_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(deptId)};

        String sortOrder = DatabaseSchema.TableProblem._ID + " ASC";
        Cursor c = mDatabase.query(
                DatabaseSchema.TableProblem.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return c;
    }
    public String getProblemName(int id){
        String[] projection = {
                DatabaseSchema.TableProblem.COLUMN_NAME
        };

        String selection = DatabaseSchema.TableProblem._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableProblem.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        String name="";
        if(c.moveToFirst()){
            name = c.getString(c.getColumnIndex(DatabaseSchema.TableProblem.COLUMN_NAME));
        }
        return name;
    }

    public void deleteProblems(){
        mDatabase.delete(DatabaseSchema.TableProblem.TABLE_NAME,null,null);
    }
}
