package com.guoliang.dnote.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.guoliang.dnote.R;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getSupportActionBar().hide();//隐藏标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
		
		//显示开始界面，2s后跳转到下一界面
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
				//如果开启了指纹解锁，则进入解锁界面，否则进入登录界面
                SharedPreferences prf = getSharedPreferences("data",MODE_PRIVATE);
                if(prf.getInt(ListActivity.KEY_EXTRA_PRINTFINGRT_STATE,ListActivity.PRINTFINGER_UNLOCK_CANCLE) == ListActivity.PRINTFINGER_UNLOCK_ENBLE){
                    Intent intent = new Intent(StartActivity.this,UnlockActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                StartActivity.this.finish();
            }},2000);
    }
}
