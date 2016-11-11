package in.andonsystem.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import in.andonsystem.database.DatabaseSchema;
import in.andonsystem.model.Issue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by Md Zahid Raza on 17/06/2016.
 */
public class IssueService {
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String TAG = IssueService.class.getSimpleName();

    private SQLiteDatabase mDatabase;

    public IssueService(SQLiteDatabase db){
        this.mDatabase = db;
    }

    public long insertIssue(Issue issue){
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        ContentValues values = new ContentValues();
        String ackAt = null;
        String fixAt = null;
        if(issue.getAckAt() != 0L){
            ackAt = df.format(new Date(issue.getAckAt()));
        }
        if(issue.getFixAt() != 0L){
            fixAt = df.format(new Date(issue.getFixAt()));
        }

        values.put(DatabaseSchema.TableIssue._ID,issue.getId());
        values.put(DatabaseSchema.TableIssue.COLUMN_LINE,issue.getLine());
        values.put(DatabaseSchema.TableIssue.COLUMN_DEPTID,issue.getDeptId());
        values.put(DatabaseSchema.TableIssue.COLUMN_SECID,issue.getSecId());
        values.put(DatabaseSchema.TableIssue.COLUMN_PROBID,issue.getProbId());
        values.put(DatabaseSchema.TableIssue.COLUMN_CRITICAL,issue.getCritical());
        values.put(DatabaseSchema.TableIssue.COLUMN_OPERATOR_NO,issue.getOperatorNo());
        values.put(DatabaseSchema.TableIssue.COLUMN_DESCRIPTION,issue.getDesc());
        values.put(DatabaseSchema.TableIssue.COLUMN_RAISED_AT,df.format(new Date(issue.getRaisedAt())));
        values.put(DatabaseSchema.TableIssue.COLUMN_ACK_AT,ackAt);
        values.put(DatabaseSchema.TableIssue.COLUMN_FIX_AT,fixAt);
        values.put(DatabaseSchema.TableIssue.COLUMN_RAISED_BY,issue.getRaisedBy());
        values.put(DatabaseSchema.TableIssue.COLUMN_ACK_BY,issue.getAckBy());
        values.put(DatabaseSchema.TableIssue.COLUMN_FIX_BY,issue.getFixBy());
        values.put(DatabaseSchema.TableIssue.COLUMN_PROCESSING_AT,issue.getProcessingAt());
        values.put(DatabaseSchema.TableIssue.COLUMN_STATUS,issue.getStatus());
        values.put(DatabaseSchema.TableIssue.COLUMN_SEEK_HELP,issue.getSeekHelp());
        long result = 0;
        try {
            result = mDatabase.insertOrThrow(DatabaseSchema.TableIssue.TABLE_NAME, null, values);
        }catch (SQLiteConstraintException e){
            //e.printStackTrace();
            String selection = DatabaseSchema.TableIssue._ID + " LIKE ?";
            String[] selectionArg = { String.valueOf(issue.getId())};
            mDatabase.update(DatabaseSchema.TableIssue.TABLE_NAME,values,selection,selectionArg);
        }
        return  result;
    }

    //Called from issue Deatail Activity
    public Cursor getIssue(int issueId){
        String[] projection = {
                DatabaseSchema.TableIssue._ID,
                DatabaseSchema.TableIssue.COLUMN_LINE,
                DatabaseSchema.TableIssue.COLUMN_SECID,
                DatabaseSchema.TableIssue.COLUMN_DEPTID,
                DatabaseSchema.TableIssue.COLUMN_PROBID,
                DatabaseSchema.TableIssue.COLUMN_CRITICAL,
                DatabaseSchema.TableIssue.COLUMN_OPERATOR_NO,
                DatabaseSchema.TableIssue.COLUMN_DESCRIPTION,
                DatabaseSchema.TableIssue.COLUMN_RAISED_AT,
                DatabaseSchema.TableIssue.COLUMN_RAISED_BY,
                DatabaseSchema.TableIssue.COLUMN_ACK_AT,
                DatabaseSchema.TableIssue.COLUMN_ACK_BY,
                DatabaseSchema.TableIssue.COLUMN_FIX_AT,
                DatabaseSchema.TableIssue.COLUMN_FIX_BY,
                DatabaseSchema.TableIssue.COLUMN_PROCESSING_AT,
                DatabaseSchema.TableIssue.COLUMN_STATUS,
                DatabaseSchema.TableIssue.COLUMN_SEEK_HELP
        };

        String selection = DatabaseSchema.TableIssue._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(issueId)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableIssue.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        return c;
    }

    public int getSeekHelp(int issueId){
        String[] projection = {DatabaseSchema.TableIssue.COLUMN_SEEK_HELP};
        String selection = DatabaseSchema.TableIssue._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(issueId)};
        Cursor c = mDatabase.query(
                DatabaseSchema.TableIssue.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();
        return c.getInt(c.getColumnIndex(DatabaseSchema.TableIssue.COLUMN_SEEK_HELP));
    }



    //Called on Home Screen
    public Cursor getIssues(int line,int secId,int deptId){
        String[] projection = {
                DatabaseSchema.TableIssue._ID,
                DatabaseSchema.TableIssue.COLUMN_LINE,
                DatabaseSchema.TableIssue.COLUMN_DEPTID,
                DatabaseSchema.TableIssue.COLUMN_PROBID,
                DatabaseSchema.TableIssue.COLUMN_CRITICAL,
                DatabaseSchema.TableIssue.COLUMN_RAISED_AT,
                DatabaseSchema.TableIssue.COLUMN_ACK_AT,
                DatabaseSchema.TableIssue.COLUMN_FIX_AT
        };
        String selection;
        String[] selectionArg;
        if(line != 0){
            if(secId != 0){
                if(deptId != 0){
                    //case 1: All filters applied
                    selection = DatabaseSchema.TableIssue.COLUMN_LINE  + " = ? AND " +
                                DatabaseSchema.TableIssue.COLUMN_SECID + " = ? AND " +
                                DatabaseSchema.TableIssue.COLUMN_DEPTID + " = ?";
                    selectionArg = new String[]{String.valueOf(line),String.valueOf(secId),String.valueOf(deptId)};
                }else{
                    //case 2: Department filter not applied
                    selection = DatabaseSchema.TableIssue.COLUMN_LINE  + " = ? AND " +
                            DatabaseSchema.TableIssue.COLUMN_SECID + " = ? ";

                    selectionArg = new String[]{String.valueOf(line),String.valueOf(secId)};
                }
            }else{
                if(deptId != 0){
                    //case 3 : Section filter not applied
                    selection = DatabaseSchema.TableIssue.COLUMN_LINE  + " = ? AND " +
                            DatabaseSchema.TableIssue.COLUMN_DEPTID + " = ?";
                    selectionArg = new String[]{String.valueOf(line),String.valueOf(deptId)};
                }else{
                    //case 4: only line filter applied
                    selection = DatabaseSchema.TableIssue.COLUMN_LINE  + " = ? ";
                    selectionArg = new String[]{String.valueOf(line)};
                }
            }

        }else{
            if(secId != 0){
                if(deptId != 0){
                    //case 5: line filter not applied
                    selection = DatabaseSchema.TableIssue.COLUMN_SECID + " = ? AND " +
                                DatabaseSchema.TableIssue.COLUMN_DEPTID + " = ?";
                    selectionArg = new String[]{String.valueOf(secId),String.valueOf(deptId)};

                }else{
                    //case 6: Only section filter applied
                    selection = DatabaseSchema.TableIssue.COLUMN_SECID + " = ?";
                    selectionArg = new String[]{String.valueOf(secId)};
                }
            }else{
                if(deptId != 0){
                    //case 7: only department filter applied
                    selection = DatabaseSchema.TableIssue.COLUMN_DEPTID + " = ?";
                    selectionArg = new String[]{String.valueOf(deptId)};

                }else{
                    //case 5: no filter applied
                    selection = null;
                    selectionArg = null;
                }
            }

        }

        Cursor c = mDatabase.query(
                DatabaseSchema.TableIssue.TABLE_NAME,
                projection,
                selection,
                selectionArg,
                null,
                null,
                null
        );
        return c;
    }

    //get Issues to which user concerned to these lines and problem
    public Cursor getIssues(String lines,String problems){
        String[] projection = {
                DatabaseSchema.TableIssue._ID,
                DatabaseSchema.TableIssue.COLUMN_DEPTID,
                DatabaseSchema.TableIssue.COLUMN_PROBID,
                DatabaseSchema.TableIssue.COLUMN_RAISED_AT,
                DatabaseSchema.TableIssue.COLUMN_ACK_AT,
                DatabaseSchema.TableIssue.COLUMN_FIX_AT,
                DatabaseSchema.TableIssue.COLUMN_RAISED_BY,
                DatabaseSchema.TableIssue.COLUMN_ACK_BY
        };
        String selection;

        if(problems.contains("(0)")){
            selection = DatabaseSchema.TableIssue.COLUMN_LINE + " IN " + lines ;
        }else{
            selection = DatabaseSchema.TableIssue.COLUMN_LINE + " IN " + lines + " AND " + DatabaseSchema.TableIssue.COLUMN_PROBID + " IN " + problems;
        }

        String[] selectionArg = {};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableIssue.TABLE_NAME,
                projection,
                selection,
                selectionArg,
                null,
                null,
                null
        );
        return c;
    }

    //Called for Notification activity
    public Cursor getIssues(int desgnId){
        String[] projection = {
                DatabaseSchema.TableIssue._ID,
                DatabaseSchema.TableIssue.COLUMN_DEPTID,
                DatabaseSchema.TableIssue.COLUMN_PROBID,
                DatabaseSchema.TableIssue.COLUMN_RAISED_AT,
                DatabaseSchema.TableIssue.COLUMN_ACK_AT,
                DatabaseSchema.TableIssue.COLUMN_FIX_AT,
                DatabaseSchema.TableIssue.COLUMN_RAISED_BY,
                DatabaseSchema.TableIssue.COLUMN_ACK_BY
        };

        String selection = DatabaseSchema.TableIssue.COLUMN_RAISED_BY + " LIKE ? ";
        String[] selectionArg = {String.valueOf(desgnId)};

        Cursor c = mDatabase.query(
                DatabaseSchema.TableIssue.TABLE_NAME,
                projection,
                selection,
                selectionArg,
                null,
                null,
                null
        );
        return c;
    }


/*
    public  Boolean updateIssue(Issue issue){
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableIssue.COLUMN_ACK_AT,df.format(new Date(issue.getAckAt())));
        values.put(DatabaseSchema.TableIssue.COLUMN_ACK_BY,issue.getAckBy());
        values.put(DatabaseSchema.TableIssue.COLUMN_SEEK_HELP,issue.getSeekHelp());
        values.put(DatabaseSchema.TableIssue.COLUMN_FIX_AT,df.format(new Date(issue.getSolvedAt())));
        values.put(DatabaseSchema.TableIssue.COLUMN_STATUS,issue.getStatus());

        String selection = DatabaseSchema.TableIssue._ID + " LIKE ?";
        String[] selectionArg = { String.valueOf(issue.getId())};

        int count = mDatabase.update(DatabaseSchema.TableIssue.TABLE_NAME,values,selection,selectionArg);
        return ( count > 0 ? true : false);
    }
    public  Boolean acknowledgeIssue(Issue issue){
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableIssue.COLUMN_ACK_AT,df.format(new Date(issue.getAckAt())));
        values.put(DatabaseSchema.TableIssue.COLUMN_ACK_BY,issue.getAckBy());

        String selection = DatabaseSchema.TableIssue._ID + " LIKE ?";
        String[] selectionArg = { String.valueOf(issue.getId())};

        int count = mDatabase.update(DatabaseSchema.TableIssue.TABLE_NAME,values,selection,selectionArg);
        return ( count > 0 ? true : false);
    }

    public  Boolean fixIssue(Issue issue){
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableIssue.COLUMN_FIX_AT,df.format(new Date(issue.getSolvedAt())));
        values.put(DatabaseSchema.TableIssue.COLUMN_STATUS,issue.getStatus());

        String selection = DatabaseSchema.TableIssue._ID + " LIKE ?";
        String[] selectionArg = { String.valueOf(issue.getId())};

        int count = mDatabase.update(DatabaseSchema.TableIssue.TABLE_NAME,values,selection,selectionArg);
        return ( count > 0 ? true : false);
    }

    public Boolean seekHelp(Issue issue){
        ContentValues values = new ContentValues();

        values.put(DatabaseSchema.TableIssue.COLUMN_SEEK_HELP,issue.getSeekHelp());

        String selection = DatabaseSchema.TableIssue._ID + " LIKE ?";
        String[] selectionArg = { String.valueOf(issue.getId())};

        int count = mDatabase.update(DatabaseSchema.TableIssue.TABLE_NAME,values,selection,selectionArg);
        return ( count > 0 ? true : false);
    }
    */
    public int deleteOldIssues(long currentTime){
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        String time = df.format(new Date(currentTime));

        String selection = DatabaseSchema.TableIssue.COLUMN_RAISED_AT + " < DATETIME('"+ time +"','start of day')";
        String[] selectionArg = {};
       return mDatabase.delete(DatabaseSchema.TableIssue.TABLE_NAME,selection,selectionArg);
    }

    public void deleteIssues(){
        mDatabase.delete(DatabaseSchema.TableIssue.TABLE_NAME,null,null);
    }
}
