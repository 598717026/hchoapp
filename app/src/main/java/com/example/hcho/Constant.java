package com.example.hcho;

public class Constant {

    public static final String CREATE_TABLE_SQL_VALUE = "create table if not exists HCHO_Value([CreatedTime] TimeStamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "'adc' integer, 'ppb' integer);";
    public static final String CREATE_TABLE_SQL_SETTING = "create table if not exists HCHO_Setting('adc_max' integer," + "'adc_min' integer," + "'val_max' integer," + "'val_min' integer);";
    public static final String CREATE_TABLE_SQL_V2 = "create table if not exists HCHO_Value(_id integer primary key," +
            "name text , age int);";
    public static final String DROP_TABLE_SQL = "drop table if exists HCHO_Value";

    public static final String DB_NAME = "hcho_db";
    //    public static int DB_VERSION = 1;
    public static final String TABLE_VALUE = "HCHO_Value";
    public static final String TABLE_SETTING = "HCHO_Setting";
    public static final String COLUMN_DATE = "sampletime";
    public static final String COLUMN_VALUE_ADC = "adc";
    public static final String COLUMN_VALUE_PPB = "ppb";

    public static final String COLUMN_ADC_MAX = "adc_max";
    public static final String COLUMN_ADC_MIN = "adc_min";
    public static final String COLUMN_VALUE_MAX = "val_max";
    public static final String COLUMN_VALUE_MIN = "val_min";

    public static final String MAX_ID = "max_id";
    public static final String VERSION = "version";
    public static final String PREF_NAME = "db_pref";
}
