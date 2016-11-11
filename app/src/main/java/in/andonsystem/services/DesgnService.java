package in.andonsystem.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.andonsystem.database.DatabaseSchema;

/**
 * Created by Md Zahid Raza on 21/06/2016.
 */
public class DesgnService {

    private SQLiteDatabase mDatabase;

    public DesgnService(SQLiteDatabase db){
        this.mDatabase = db;
    }

    public long saveDesgn(int desgnId,String name){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableDesgn._ID,desgnId);
        values.put(DatabaseSchema.TableDesgn.COLUMN_NAME,name);

        long id = mDatabase.insert(DatabaseSchema.TableDesgn.TABLE_NAME,null,values);
        return id;
    }

    public long saveDesgnLine(int desgnId,int line){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableDesgnLine.COLUMN_DESGN_ID,desgnId);
        values.put(DatabaseSchema.TableDesgnLine.COLUMN_LINE,line);

        long id = mDatabase.insert(DatabaseSchema.TableDesgnLine.TABLE_NAME,null,values);
        return id;
    }

    public long saveDesgnProblem(int desgnId,int probId){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableDesgnProblem.COLUMN_DESGN_ID,desgnId);
        values.put(DatabaseSchema.TableDesgnProblem.COLUMN_PROB_ID,probId);

        long id = mDatabase.insert(DatabaseSchema.TableDesgnProblem.TABLE_NAME,null,values);
        return id;
    }

    public String getDesgnName(int desgnId){
        String[] projection = {
                DatabaseSchema.TableDesgn.COLUMN_NAME
        };

        String selection = DatabaseSchema.TableDesgn._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(desgnId)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableDesgn.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        String name="";
        if(c.moveToFirst()){
            name = c.getString(c.getColumnIndex(DatabaseSchema.TableDesgn.COLUMN_NAME));
        }
        return name;
    }

    public Cursor getDesgns(){
        String[] projection = {
                DatabaseSchema.TableDesgn.COLUMN_NAME,
                DatabaseSchema.TableDesgn._ID
        };

        return mDatabase.query(DatabaseSchema.TableDesgn.TABLE_NAME,projection,null,null,null,null,null);
    }

    public Boolean isConcernedDesgn(int desgnId,int line,int probId){
        Boolean status = false;
        String [] projection1 = {
                DatabaseSchema.TableDesgnProblem.COLUMN_DESGN_ID
        };
        String selection1 = DatabaseSchema.TableDesgnProblem.COLUMN_DESGN_ID + " LIKE ? AND "
                + DatabaseSchema.TableDesgnProblem.COLUMN_PROB_ID  + " LIKE ?";
        String[] selectionArg1 = {String.valueOf(desgnId),String.valueOf(probId)};

        Cursor c = mDatabase.query(DatabaseSchema.TableDesgnProblem.TABLE_NAME,projection1,selection1,selectionArg1,null,null,null);
        if(c.moveToFirst()){
            String [] projection2 = {
                    DatabaseSchema.TableDesgnLine.COLUMN_DESGN_ID
            };
            String selection2 = DatabaseSchema.TableDesgnLine.COLUMN_DESGN_ID + " LIKE ? AND "
                    + DatabaseSchema.TableDesgnLine.COLUMN_LINE  + " IN (0, ?)";
            String[] selectionArg2 = {String.valueOf(desgnId),String.valueOf(line)};
            Cursor c2 = mDatabase.query(DatabaseSchema.TableDesgnLine.TABLE_NAME,projection2,selection2,selectionArg2,null,null,null);
            if(c2.moveToFirst()){
                status = true;
            }
        }
        return status;
    }

    public String getUserLines(int desgnId){
        StringBuilder lines = new StringBuilder();
        lines.append("(");
        String[] projection = {
                DatabaseSchema.TableDesgnLine.COLUMN_LINE
        };

        String selection = DatabaseSchema.TableDesgnLine.COLUMN_DESGN_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(desgnId)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableDesgnLine.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if(c.getCount() == 1){
            c.moveToFirst();
            int temp = c.getInt(c.getColumnIndex(DatabaseSchema.TableDesgnLine.COLUMN_LINE));
            if(temp == 0){
                lines.append("1,2,3,4,5,6,7,8");
            }
            else {
                lines.append(String.valueOf(temp));
            }
        }else if(c.getCount() > 1){
            c.moveToFirst();
            lines.append(String.valueOf(c.getInt(c.getColumnIndex(DatabaseSchema.TableDesgnLine.COLUMN_LINE))));
            while (c.moveToNext()) {
                lines.append("," + String.valueOf(c.getInt(c.getColumnIndex(DatabaseSchema.TableDesgnLine.COLUMN_LINE))));
            }
        }
        lines.append(")");
        return lines.toString();
    }

    public String getUserProblems(int desgnId){
        StringBuilder problems = new StringBuilder();
        problems.append("(");
        String[] projection = {
                DatabaseSchema.TableDesgnProblem.COLUMN_PROB_ID
        };

        String selection = DatabaseSchema.TableDesgnProblem.COLUMN_DESGN_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(desgnId)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableDesgnProblem.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if(c.getCount() > 0) {
            c.moveToFirst();
            problems.append(String.valueOf(c.getInt(c.getColumnIndex(DatabaseSchema.TableDesgnProblem.COLUMN_PROB_ID))));
            while (c.moveToNext()) {
                problems.append("," + String.valueOf(c.getInt(c.getColumnIndex(DatabaseSchema.TableDesgnProblem.COLUMN_PROB_ID))));
            }
        }
        problems.append(")");
        return problems.toString();
    }

    public void deleteDesgns(){
        mDatabase.delete(DatabaseSchema.TableDesgn.TABLE_NAME,null,null);
        mDatabase.delete(DatabaseSchema.TableDesgnLine.TABLE_NAME,null,null);
        mDatabase.delete(DatabaseSchema.TableDesgnProblem.TABLE_NAME,null,null);
    }

}
