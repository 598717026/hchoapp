package com.example.hcho;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import cz.msebera.android.httpclient.Header;


public class FunInterfaceActivity extends Activity {

    private MySqliteOpenHelper sqliteOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fun_interface);

        Button mButton = findViewById(R.id.buttonCal);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent = intent.setClass(FunInterfaceActivity.this, CalActivity.class);
                startActivity(intent);
            }
        });

        Button mButtonClear = findViewById(R.id.buttonClear);
        mButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 清除数据
                new AlertDialog.Builder(FunInterfaceActivity.this)
                        .setTitle("是否真的清除数据？")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                sqliteOpenHelper.clearValues();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        Button mButtonUpload = findViewById(R.id.buttonUpload);
        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 上传数据
                upLoadFile(FunInterfaceActivity.this, String.valueOf(getDatabasePath(Constant.DB_NAME)), "");
            }
        });

        TextView textView = findViewById(R.id.textViewMac);
        try {
            textView.setText("WifiMac:" + getMac());
        } catch (SocketException e) {
            e.printStackTrace();
        }

        sqliteOpenHelper = new MySqliteOpenHelper(this, Constant.DB_NAME, 1);

    }

    /**
     * 文件转base64字符串
     *
     * @param file
     * @return
     */
    public static String fileToBase64(String file) {
        Log.e("httptest", "fileToBase64:" + file);
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return base64;
    }

    public static void upLoadFile(final Context cont, String filename, String regData) {
        try {
            //将图片的字节流数据加密成base64字符输出
            String data = fileToBase64(filename);

            Log.e("httptest", "data:" + data.length());
            //photo=URLEncoder.encode(photo,"UTF-8");
            RequestParams params = new RequestParams();
            params.put("data", data);
            params.put("mac", getMac());//传输的字符数据
//            Log.e("httptest", "" + params);

            String url = "http://119.28.19.224:8888/file";


            AsyncHttpClient client = new AsyncHttpClient();
            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    Toast.makeText(cont, "上传成功! 可以清除数据，以免重复上传", 0)
                            .show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    Toast.makeText(cont, "上传失败!" + responseBody, 0)
                            .show();
                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getMac() throws SocketException {
        String wifimac = "";
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements())
        {
            NetworkInterface iF = interfaces.nextElement();

            byte[] addr = iF.getHardwareAddress();
            if (addr == null || addr.length == 0)
            {
                continue;
            }

            StringBuilder buf = new StringBuilder();
            for (byte b : addr)
            {
                buf.append(String.format("%02X", b));
            }
//            if (buf.length() > 0)
//            {
//                buf.deleteCharAt(buf.length() - 1);
//
//            }
            String mac = buf.toString();
            if (iF.getName().equals("wlan0"))
            {
                wifimac = mac;
            }
            Log.e("getMac", iF.getName() + ":mac:" + mac);
        }

        return wifimac;
    }
}