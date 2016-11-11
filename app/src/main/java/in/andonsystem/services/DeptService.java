package in.andonsystem.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.andonsystem.database.DatabaseSchema;

/**
 * Created by Administrator on 01-06-2016.
 */
public class DeptService {

    private SQLiteDatabase mDatabase;

    public DeptService(SQLiteDatabase db){
        this.mDatabase = db;
    }

    public long insertDept(int deptId,String name){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableDepartment._ID,deptId);
        values.put(DatabaseSchema.TableDepartment.COLUMN_NAME,name);

        long result = mDatabase.insert(DatabaseSchema.TableDepartment.TABLE_NAME,null,values);
        return result;
    }

    public Cursor getDepts(){
        String[] projection = {
                DatabaseSchema.TableDepartment._ID,
                DatabaseSchema.TableDepartment.COLUMN_NAME
        };

        String sortOrder = DatabaseSchema.TableSection._ID ;
        Cursor c = mDatabase.query(
                DatabaseSchema.TableDepartment.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        return c;
    }

    public String getDeptName(int id){
        String[] projection = {
                DatabaseSchema.TableDepartment.COLUMN_NAME
        };

        String selection = DatabaseSchema.TableDepartment._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableDepartment.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        String name="";
        if(c.moveToFirst()){
            name = c.getString(c.getColumnIndex(DatabaseSchema.TableDepartment.COLUMN_NAME));
        }
        return name;
    }

    public void deleteDepts(){
        mDatabase.delete(DatabaseSchema.TableDepartment.TABLE_NAME,null,null);
    }
}
