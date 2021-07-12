package com.example.hcho;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

public class DataService extends Service {
    private static final int ONE_Miniute = 1 *1000;
    private static final int PENDING_REQUEST = 0;

    private static final String HCHO_SERVICE = "xtchcho";

    private MySqliteOpenHelper sqliteOpenHelper;

    Object hchoService = null;
    Timer timer = null;
    TimerTask task = null;

    public DataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sqliteOpenHelper = new MySqliteOpenHelper(this, Constant.DB_NAME, 1);

        Log.e(this.getClass().getSimpleName(), "onCreate");
        hchoService = this.getSystemService(HCHO_SERVICE);

        Log.e(this.getClass().getSimpleName(), "onCreate end");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(this.getClass().getSimpleName(), "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //通过AlarmManager定时启动广播
        AlarmManager alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime= SystemClock.elapsedRealtime()+ONE_Miniute;//从开机到现在的毫秒书（手机睡眠(sleep)的时间也包括在内
        Intent i=new Intent(this, AlarmReceive.class);
        PendingIntent pIntent= PendingIntent.getBroadcast(this,PENDING_REQUEST,i,PENDING_REQUEST);
        alarmManager.set(AlarmManager.RTC_WAKEUP,triggerAtTime,pIntent);

        sqliteOpenHelper.insert(getHchoAdc());

        Log.e(this.getClass().getSimpleName(), "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    public int getHchoAdc()
    {
        if (hchoService == null)
        {
            return 0;
        }

        Object invoke = 0;
        try {
            invoke = hchoService.getClass().getDeclaredMethods()[0].invoke(hchoService, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return (int) invoke;
    }

}