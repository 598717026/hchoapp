package com.example.hcho;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
            }
        });

        sqliteOpenHelper = new MySqliteOpenHelper(this, Constant.DB_NAME, 1);

    }
}