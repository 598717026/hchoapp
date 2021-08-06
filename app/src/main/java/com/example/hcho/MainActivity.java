package com.example.hcho;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

//import org.apache.commons.math3.complex.Complex;
//import org.apache.commons.math3.transform.DftNormalization;
//import org.apache.commons.math3.transform.FastFourierTransformer;
//import org.apache.commons.math3.transform.TransformType;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    final int PPB_LOW = 40;
    final int PPB_DANGEROUS = 57;

    final String PPB_LOW_TEXT = "正常";
    final String PPB_WANNING_TEXT = "警告";
    final String PPB_DANGEROUS_TEXT = "危险";

    final int PPB_LOW_COLOR = 0x4CAF50;
    final int PPB_WANNING_COLOR = 0xff8800;
    final int PPB_DANGEROUS_COLOR = 0xff0000;

    Timer timer = null;
    TimerTask task = null;

    private MySqliteOpenHelper sqliteOpenHelper;

    private ContentResolver mContentResolver = null;
    private LineChartView lineChart;

    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();


    final int POINT_NUM = 30;

    final int CLICK_MAX = 10;
    private int mClickNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //连续启动Service
        Intent intentOne = new Intent(this, DataService.class);
        startService(intentOne);

        lineChart = (LineChartView)findViewById(R.id.line_chart);
        initLineChart();

        lineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickNum == 0)
                {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             *要执行的操作
                             */
                            mClickNum = 0;
                        }
                    }, 5000);
                }
                mClickNum += 1;
                if (mClickNum >= CLICK_MAX)
                {
                    mClickNum = 0;
                    Intent intent = new Intent();
                    intent = intent.setClass(MainActivity.this, FunInterfaceActivity.class);
                    startActivity(intent);
                }
            }
        });

//        Button mButton = findViewById(R.id.buttonCal);
//        mButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent = intent.setClass(MainActivity.this, CalActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        Button mButtonClear = findViewById(R.id.buttonClear);
//        mButtonClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 清除数据
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("是否真的清除数据？")
//                        .setIcon(android.R.drawable.ic_dialog_info)
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface arg0, int arg1) {
//                                sqliteOpenHelper.clearValues();
//                            }
//                        })
//                        .setNegativeButton("取消", null)
//                        .show();
//            }
//        });
//
//        Button mButtonUpload = findViewById(R.id.buttonUpload);
//        mButtonUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 上传数据
//            }
//        });

        mContentResolver = getContentResolver();

        sqliteOpenHelper = new MySqliteOpenHelper(this, Constant.DB_NAME, 1);


    }

    @Override
    protected void onPause() {
        super.onPause();

        timer.cancel();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initTimerTask();
    }

    private void initTimerTask()
    {

        // 每秒对采样一次
        timer = new Timer();
        task = new TimerTask() {
            int counter = 0;
            @Override
            public void run() {

                int adc = sqliteOpenHelper.getAdc();
                Log.d(getClass().getName(), "xtchcho：" + adc);
                int val = sqliteOpenHelper.getPpb();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Message message = new Message();

                        message.what = 1;
                        message.arg1 = adc;
                        message.arg2 = val;

                        mHandlerValue.sendMessage(message);
                    }
                }).start();
            }
        };
        timer.schedule(task,1000,1000);

    }

    private Handler mHandlerValue = new Handler() {

        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage");
            TextView ppbTextView = findViewById(R.id.textViewPpb);
            TextView fftPpbTextView = findViewById(R.id.FFTtextViewPpb);
            TextView hchoTextView = findViewById(R.id.textViewHchoStatus);

            switch (msg.what) {

                case 1:
                    int adc =  msg.arg1;
                    int ppb = msg.arg2;
//                    ppbTextView.setText(String.format("    %s(ppb)", msg.arg1));
//                    fftPpbTextView.setText("    " + FFTDirect(msg.arg1) + "(ppb)");
//                    int v = filterTest(msg.arg1);
                    ppbTextView.setText(String.format("    %s(ppb)", ppb));
                    fftPpbTextView.setText("    " + ppb + "(ppb)");
                    int v = ppb;
                    if (v < 0)
                    {
                        ppbTextView.setBackgroundColor(Color.YELLOW);
                    }
                    else {
                        ppbTextView.setBackgroundColor(Color.GREEN);

                        if (v < PPB_LOW) {
                            hchoTextView.setText(PPB_LOW_TEXT);
                            hchoTextView.setBackgroundColor(Color.GREEN);
                        } else if (v < PPB_DANGEROUS) {
                            hchoTextView.setText(PPB_WANNING_TEXT);
                            hchoTextView.setBackgroundColor(Color.YELLOW);
                        } else {
                            hchoTextView.setText(PPB_DANGEROUS_TEXT);
                            hchoTextView.setBackgroundColor(Color.RED);
                        }
                    }

                    addLineChartData(ppb);
                    setLineChartData();
                    break;

                default:

                    break;

            }

        }

    };

    public void addLineChartData(float v)
    {
        if (mPointValues.size() == 0)
        {
            if (v > 1) {
                mPointValues.add(new PointValue(0, v - 1));
            }
            else
            {
                mPointValues.add(new PointValue(0, 0));
            }
        }
        Log.d(getClass().getName(), "addLineChartData1：" + mPointValues.size());
        mPointValues.add(new PointValue(mPointValues.size(), v));
        Log.d(getClass().getName(), "addLineChartData2：" + mPointValues.size());
        if (mPointValues.size() > POINT_NUM)
        {
            mPointValues.remove(0);
            for (int i = 0; i < mPointValues.size(); i++)
            {
                PointValue iv = mPointValues.get(i);
                iv.set(i, iv.getY());
            }
            // 解决所有值一样显示不了问题，隐藏第一个默认值
            PointValue iv = mPointValues.get(0);
            PointValue iv1 = mPointValues.get(1);
            iv.set(0, iv1.getY() - 1);
        }
        Log.d(getClass().getName(), "addLineChartData3：" + mPointValues.size());
    }

    public void setLineChartData() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);    //折线图上每个数据点的形状，这里是圆形
        line.setCubic(false);
        line.setFilled(false);
        line.setHasLabels(false);
        line.setHasLines(true);
        line.setHasPoints(false);
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.parseColor("#565659"));//设置字体颜色

        axisX.setTextSize(8);//设置字体大小
        axisX.setMaxLabelChars(8);//最多几个X轴坐标
        axisX.setValues(mAxisXValues);
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);


        Axis axisY = new Axis();
        axisY.setName("");
        axisY.setTextSize(8);
        axisY.setTextColor(Color.parseColor("#565659"));//设置字体颜色
        data.setAxisYLeft(axisY);
        //设置行为属性，缩放、滑动、平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 3);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        //设置X轴数据的显示个数（x轴0-7个数据）
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        // 解决所有值一样显示不了问题，隐藏第一个默认值
        v.left = 2;
        v.right= POINT_NUM;
        lineChart.setCurrentViewport(v);
    }

    private void initLineChart(){
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);    //折线图上每个数据点的形状，这里是圆形
        line.setCubic(false);
        line.setFilled(false);
        line.setHasLabels(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis();
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.parseColor("#D6D6D9"));//设置字体颜色

        axisX.setTextSize(8);//设置字体大小
        axisX.setMaxLabelChars(8);//最多几个X轴坐标
        axisX.setValues(mAxisXValues);
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);


        Axis axisY = new Axis();
        axisY.setName("");
        axisY.setTextSize(8);
        data.setAxisYLeft(axisY);
        //设置行为属性，缩放、滑动、平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 3);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        //设置X轴数据的显示个数（x轴0-7个数据）
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        lineChart.setCurrentViewport(v);
    }

//    final int DO_LENGHT = 120;
//    final int FFT_LENGHT = 512;
//    FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
//
//    double [] fftDirtectData = new double[FFT_LENGHT];
//    int fftDirtectLen = 0;
//    int fftDiretctCount = 0;
//    double fftValue = 0;
//
//    public void FFTDirectInit()
//    {
//        fftDirtectLen = 0;
//        fftDiretctCount = 0;
//        fftValue = 0;
//    }
//
//    public double FFTDirect(double v)
//    {
//        for (int i = 0; i < fftDirtectData.length - 1; i++)
//        {
//            fftDirtectData[i] = fftDirtectData[i + 1];
//        }
//        fftDirtectData[fftDirtectData.length-1] = v;
//        fftDiretctCount++;
//        fftDirtectLen++;
//        if (fftDiretctCount >= DO_LENGHT)
//        {
//            Complex[] result = fft.transform(fftDirtectData, TransformType.FORWARD);
//            for (int i = 0; i < result.length; i++)
//            {
////                Log.d(getPackageName(), "result[" + i + "] = " + result[i]);
//                if (i > 0)
//                {
//                    result[i] = new Complex(0, 0);
//                }
//            }
//            Complex[] result1 = fft.transform(result, TransformType.INVERSE);
//            fftValue = result1[0].abs();
//            fftDiretctCount = 0;
//            fftDirtectLen = FFT_LENGHT;
//        }
//        return fftValue;
//    }

}