package in.andonsystem.database;

import android.provider.BaseColumns;

/**
 * Created by Md Zahid Raza on 29-02-2016.
 */
public final class DatabaseSchema {

    public static final String DATABASE_NAME = "andon";
    public static final int DATABASE_VERSION = 2;
    public static final String TYPE_VARCHAR = " VARCHAR(255)";
    public static final String TYPE_INTEGER = " INTEGER";
    public static final String TYPE_TIMESTAMP = " TIMESTAMP";
    public static final String COMMA_SEP = ",";

    public DatabaseSchema(){}

    public static abstract class TableDepartment implements BaseColumns{
        public static final String TABLE_NAME = "department";
        public static final String COLUMN_NAME = "name";

        public static final String CREATE_TABLE = "CREATE TABLE "
                + TABLE_NAME + "(" +
                _ID + TYPE_INTEGER + " PRIMARY KEY," +
                COLUMN_NAME + TYPE_VARCHAR +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static abstract class TableSection implements BaseColumns{
        public static final String TABLE_NAME = "section";
        public static final String COLUMN_NAME = "name";

        public static final String CREATE_TABLE = "CREATE TABLE "
                + TABLE_NAME + "(" +
                    _ID + TYPE_INTEGER + " PRIMARY KEY" + COMMA_SEP +
                    COLUMN_NAME + TYPE_VARCHAR +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
    
    public static abstract class TableProblem implements BaseColumns{
        public static final String TABLE_NAME = "problem";
        public static final String COLUMN_DEPT_ID = "dept_id";
        public static final String COLUMN_NAME = "name";

        public static final String CREATE_TABLE = "CREATE TABLE "
                + TABLE_NAME + "(" +
                _ID + TYPE_INTEGER + " PRIMARY KEY" + COMMA_SEP +
                COLUMN_DEPT_ID + TYPE_INTEGER + COMMA_SEP +
                COLUMN_NAME + TYPE_VARCHAR +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static abstract class TableIssue implements BaseColumns{
        public static final String TABLE_NAME = "issue";
        public static final String COLUMN_LINE = "line";
        public static final String COLUMN_SECID = "sec_id";
        public static final String COLUMN_DEPTID = "dept_id";
        public static final String COLUMN_PROBID = "prob_id";
        public static final String COLUMN_CRITICAL = "critical";
        public static final String COLUMN_OPERATOR_NO = "operator_no";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_RAISED_AT = "raised_at";
        public static final String COLUMN_ACK_AT = "ack_at";
        public static final String COLUMN_FIX_AT = "fix_at";
        public static final String COLUMN_RAISED_BY = "raised_by";
        public static final String COLUMN_ACK_BY = "ack_by";
        public static final String COLUMN_FIX_BY = "fix_by";
        public static final String COLUMN_PROCESSING_AT = "processing_at";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_SEEK_HELP = "seek_help";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + "(" +
                _ID + TYPE_INTEGER + " PRIMARY KEY " + COMMA_SEP +
                COLUMN_LINE + TYPE_INTEGER  + COMMA_SEP +
                COLUMN_SECID + TYPE_INTEGER + COMMA_SEP +
                COLUMN_DEPTID + TYPE_INTEGER + COMMA_SEP +
                COLUMN_PROBID + TYPE_INTEGER + COMMA_SEP +
                COLUMN_CRITICAL + " VARCHAR(5) " + COMMA_SEP +
                COLUMN_OPERATOR_NO + TYPE_VARCHAR + COMMA_SEP +
                COLUMN_DESCRIPTION + TYPE_VARCHAR + COMMA_SEP +
                COLUMN_RAISED_AT + TYPE_TIMESTAMP + COMMA_SEP +
                COLUMN_ACK_AT + TYPE_TIMESTAMP + COMMA_SEP +
                COLUMN_FIX_AT + TYPE_TIMESTAMP + COMMA_SEP +
                COLUMN_RAISED_BY + TYPE_INTEGER + COMMA_SEP +
                COLUMN_ACK_BY + TYPE_INTEGER + COMMA_SEP +
                COLUMN_FIX_BY + TYPE_INTEGER + COMMA_SEP +
                COLUMN_PROCESSING_AT + " INTEGER(2) " + COMMA_SEP +
                COLUMN_STATUS + " INTEGER(2) " + COMMA_SEP +
                COLUMN_SEEK_HELP + " INTEGER(2) " +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public static abstract class TableUser implements BaseColumns{
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_DESGN_ID = "desgn_id";
        public static final String COLUMN_MOBILE = "mobile";
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + "(" +
                _ID + TYPE_INTEGER + " PRIMARY KEY " + COMMA_SEP +
                COLUMN_USERNAME + TYPE_VARCHAR + COMMA_SEP +
                COLUMN_DESGN_ID + TYPE_INTEGER + COMMA_SEP +
                COLUMN_MOBILE + TYPE_VARCHAR +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TableDesgn implements BaseColumns{
        public static final String TABLE_NAME = "desgn";
        public static final String COLUMN_NAME = "name";
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + "(" +
                _ID + TYPE_INTEGER + " PRIMARY KEY " + COMMA_SEP +
                COLUMN_NAME + TYPE_VARCHAR +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TableDesgnLine{
        public static final String TABLE_NAME = "desgn_line";
        public static final String COLUMN_DESGN_ID = "desgn_id";
        public static final String COLUMN_LINE = "line";
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + "(" +
                COLUMN_DESGN_ID + TYPE_INTEGER + COMMA_SEP +
                COLUMN_LINE + TYPE_INTEGER +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class TableDesgnProblem{
        public static final String TABLE_NAME = "desgn_problem";
        public static final String COLUMN_DESGN_ID = "desgn_id";
        public static final String COLUMN_PROB_ID = "prob_id";
        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + "(" +
                COLUMN_DESGN_ID + TYPE_INTEGER + COMMA_SEP +
                COLUMN_PROB_ID + TYPE_INTEGER +
                ")";
        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }


}
