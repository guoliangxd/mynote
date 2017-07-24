package com.guoliang.dnote.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.guoliang.dnote.R;
import com.guoliang.dnote.global.FingerprintUtil;

public class UnlockActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        mContext = this;
        unlock();
    }

    private void unlock(){
        FingerprintUtil.callFingerPrint(new FingerprintUtil.OnCallBackListenr() {
            AlertDialog dialog;
            @Override
            public void onSupportFailed() {
                Toast.makeText(UnlockActivity.this,R.string.device_not_support_printfinger,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInsecurity() {
                Toast.makeText(UnlockActivity.this,R.string.device_out_of_protect,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEnrollFailed() {
                Toast.makeText(UnlockActivity.this,R.string.set_printfinger_in_settings,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationStart() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                View view = LayoutInflater.from(mContext).inflate(R.layout.layout_fingerprint,null);
                initView(view);
                builder.setView(view);
                builder.setCancelable(false);
                builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.removeMessages(0);
                        FingerprintUtil.cancel();
                    }
                });
                dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                Toast.makeText(UnlockActivity.this,errString.toString(),Toast.LENGTH_SHORT).show();
                if (dialog != null  &&dialog.isShowing()){
                    dialog.dismiss();
                    handler.removeMessages(0);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(UnlockActivity.this,R.string.unlock_failed,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                Toast.makeText(UnlockActivity.this,helpString.toString(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                Toast.makeText(UnlockActivity.this,R.string.unlock_success,Toast.LENGTH_SHORT).show();
                if (dialog != null  &&dialog.isShowing()){
                    dialog.dismiss();
                    handler.removeMessages(0);
                }
                Intent intent = new Intent(UnlockActivity.this,LoginActivity.class);
                startActivity(intent);
                UnlockActivity.this.finish();
            }
        });
    }




    private Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                int i = postion % 5;
                if (i == 0){
                    tv[4].setBackground(null);
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                else{
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    tv[i-1].setBackground(null);
                }
                postion++;
                handler.sendEmptyMessageDelayed(0,100);
            }
        }
    };
    TextView[] tv = new TextView[5];
    private int postion = 0;
    private void initView(View view) {
        postion = 0;
        tv[0] = (TextView) view.findViewById(R.id.tv_1);
        tv[1] = (TextView) view.findViewById(R.id.tv_2);
        tv[2] = (TextView) view.findViewById(R.id.tv_3);
        tv[3] = (TextView) view.findViewById(R.id.tv_4);
        tv[4] = (TextView) view.findViewById(R.id.tv_5);
        handler.sendEmptyMessageDelayed(0,100);
    }
}
