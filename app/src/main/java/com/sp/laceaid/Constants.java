package com.sp.laceaid;

public class Constants {
    public static final String SHARED_PREFS = "laceAidSharedPrefs";
    public static final String AR_MANUAL_SHOW = "arManualShow";
    public static final String HOME_MANUAL_SHOW = "homeManualShow";

    public static final String DB_NAME = "LACE_AID_DB";
    public static final int DB_VERSION = 1;
    public static final String TABLE_NAME = "RUN_RECORD_TABLE";
    public static final String C_ID = "ID";
    public static final String C_USERNAME = "USERNAME";
    public static final String C_TIMEELAPSED = "TIMEELAPSED";
    public static final String C_TOTALDIST = "TOTALDIST";


    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + C_USERNAME + " TEXT,"
            + C_TIMEELAPSED + " TEXT,"
            + C_TOTALDIST + " TEXT"
            + ");";
}
