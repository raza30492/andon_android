package in.andonsystem.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.andonsystem.database.DatabaseSchema;

/**
 * Created by Administrator on 31-05-2016.
 */
public class SectionService {
    private SQLiteDatabase mDatabase;

    public SectionService(SQLiteDatabase db){
        this.mDatabase = db;
    }

    public long insertSection(int secId,String name){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableSection._ID,secId);
        values.put(DatabaseSchema.TableSection.COLUMN_NAME,name);

        long result = mDatabase.insert(DatabaseSchema.TableSection.TABLE_NAME,null,values);
        return result;
    }

    public Cursor getSections(){
        String[] projection = {
                DatabaseSchema.TableSection._ID,
                DatabaseSchema.TableSection.COLUMN_NAME
        };

        String sortOrder = DatabaseSchema.TableSection._ID ;
        Cursor c = mDatabase.query(
                DatabaseSchema.TableSection.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
                );
        return c;
    }

    public String getSectionName(int id){
        String[] projection = {
                DatabaseSchema.TableDepartment.COLUMN_NAME
        };

        String selection = DatabaseSchema.TableSection._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableSection.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        String name="";
        if(c.moveToFirst()){
            name = c.getString(c.getColumnIndex(DatabaseSchema.TableSection.COLUMN_NAME));
        }
        return name;
    }

    public void deleteSections(){
        mDatabase.delete(DatabaseSchema.TableSection.TABLE_NAME,null,null);
    }
}
