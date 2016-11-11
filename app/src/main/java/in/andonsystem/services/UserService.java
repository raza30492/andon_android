package in.andonsystem.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.andonsystem.database.DatabaseSchema;

/**
 * Created by Md Zahid Raza on 21/06/2016.
 */
public class UserService {

    private SQLiteDatabase mDatabase;

    public UserService(SQLiteDatabase db){
        this.mDatabase = db;
    }

    public long saveUser(int userId,String username,int desgnId,String mobile){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableUser._ID,userId);
        values.put(DatabaseSchema.TableUser.COLUMN_USERNAME,username);
        values.put(DatabaseSchema.TableUser.COLUMN_DESGN_ID,desgnId);
        values.put(DatabaseSchema.TableUser.COLUMN_MOBILE,mobile);

        long id = mDatabase.insert(DatabaseSchema.TableUser.TABLE_NAME,null,values);
        return  id;
    }

    public String getUserName(int userId){
        String[] projection = {
                DatabaseSchema.TableUser.COLUMN_USERNAME
        };

        String selection = DatabaseSchema.TableUser._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableUser.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        String name="";
        if(c.moveToFirst()){
            name = c.getString(c.getColumnIndex(DatabaseSchema.TableUser.COLUMN_USERNAME));
        }
        return name;
    }

    public void deleteUsers(){
        mDatabase.delete(DatabaseSchema.TableUser.TABLE_NAME,null,null);
    }
}
