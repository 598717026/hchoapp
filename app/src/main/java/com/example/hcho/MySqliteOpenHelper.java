package com.example.hcho;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by wangjinfa on 2017/6/3.
 */

public class MySqliteOpenHelper extends SQLiteOpenHelper {

    public static final String TAG = MySqliteOpenHelper.class.getName();
    private Context context;

    public MySqliteOpenHelper(Context context, String databaseName, int version) {
        super(context, databaseName, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Constant.CREATE_TABLE_SQL_VALUE);
        sqLiteDatabase.execSQL(Constant.CREATE_TABLE_SQL_SETTING);

        //SharedPreferences sp = context.getSharedPreferences(Constant.PREF_NAME, Context.MODE_PRIVATE);
        //sp.edit().putInt(Constant.MAX_ID, 4).commit();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            //sqLiteDatabase.execSQL("ALTER TABLE " + Constant.TABLE_NAME + " ADD COLUMN age INTEGER DEFAULT 20");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public void insert(int value) {
        int ppb = sampleToPpb(value);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constant.COLUMN_VALUE_ADC, value);


        values.put(Constant.COLUMN_VALUE_PPB, ppb);
        db.insert(Constant.TABLE_VALUE, null, values);
        db.close();
    }

    public void clearValues() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constant.TABLE_VALUE, null, null);
        db.close();
    }


    int sampleToPpb(int adc)
    {
        int adcMax = getAdcMax();
        int adcMin = getAdcMin();
        int valMax = getValueMax();
        int valMin = getValueMin();

        if (adcMax != 0) {
            return  ((adc - adcMin) * (valMax - valMin)) / (adcMax - adcMin) + valMin;
        }
        else
        {
            return adc;
        }
    }


    public void updateSetting(int adc_max, int adc_min, int val_max, int val_min) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constant.TABLE_SETTING, null, null);
        ContentValues values = new ContentValues();
        values.put(Constant.COLUMN_ADC_MAX, adc_max);
        values.put(Constant.COLUMN_ADC_MIN, adc_min);
        values.put(Constant.COLUMN_VALUE_MAX, val_max);
        values.put(Constant.COLUMN_VALUE_MIN, val_min);
        db.insert(Constant.TABLE_SETTING, null, values);
        db.close();
    }

    public int getAdc()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(Constant.TABLE_VALUE, null, null, null, null,null, "CreatedTime desc", "1");
        int adc = 0;
        int ppb = 0;
        while (cursor.moveToNext()) {
            adc = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_ADC));
            ppb = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_PPB));
            //输出查询结果
            Log.e(this.getClass().getName(),"查询到的数据是:"+adc+","+ppb);

        }
        cursor.close();
        db.close();
        return adc;
    }

    public int getPpb()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(Constant.TABLE_VALUE, null, null, null, null,null, "CreatedTime desc", "1");
        int adc = 0;
        int ppb = 0;
        while (cursor.moveToNext()) {
            adc = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_ADC));
            ppb = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_PPB));
            //输出查询结果
//            Log.e(this.getClass().getName(),"查询到的数据是:"+adc+","+ppb);

        }
        cursor.close();
        db.close();
        return ppb;
    }

    public int getAdcMax()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(Constant.TABLE_SETTING, null, null, null, null,null, null);
        int adc_max = 0;
        int adc_min = 0;
        int val_max = 0;
        int val_min = 0;
        while (cursor.moveToNext()) {
            adc_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MAX));
            adc_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MIN));
            val_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MAX));
            val_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MIN));
            //输出查询结果
//            Log.e(this.getClass().getName(),"查询到的数据是:"+adc_max+","+adc_min+","+val_max+","+val_min+",");

        }
        cursor.close();
        db.close();
        return adc_max;
    }

    public int getAdcMin()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(Constant.TABLE_SETTING, null, null, null, null,null, null);
        int adc_max = 0;
        int adc_min = 0;
        int val_max = 0;
        int val_min = 0;
        while (cursor.moveToNext()) {
            adc_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MAX));
            adc_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MIN));
            val_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MAX));
            val_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MIN));
            //输出查询结果
//            Log.e(this.getClass().getName(),"查询到的数据是:"+adc_max+","+adc_min+","+val_max+","+val_min+",");

        }
        cursor.close();
        db.close();
        return adc_min;
    }

    public int getValueMax()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(Constant.TABLE_SETTING, null, null, null, null,null, null);
        int adc_max = 0;
        int adc_min = 0;
        int val_max = 0;
        int val_min = 0;
        while (cursor.moveToNext()) {
            adc_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MAX));
            adc_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MIN));
            val_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MAX));
            val_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MIN));
            //输出查询结果
//            Log.e(this.getClass().getName(),"查询到的数据是:"+adc_max+","+adc_min+","+val_max+","+val_min+",");

        }
        cursor.close();
        db.close();
        return val_max;
    }

    public int getValueMin()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(Constant.TABLE_SETTING, null, null, null, null,null, null);
        int adc_max = 0;
        int adc_min = 0;
        int val_max = 0;
        int val_min = 0;
        while (cursor.moveToNext()) {
            adc_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MAX));
            adc_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_ADC_MIN));
            val_max = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MAX));
            val_min = cursor.getInt(cursor.getColumnIndex(Constant.COLUMN_VALUE_MIN));
            //输出查询结果
//            Log.e(this.getClass().getName(),"查询到的数据是:"+adc_max+","+adc_min+","+val_max+","+val_min+",");

        }
        cursor.close();
        db.close();
        return val_min;
    }

}