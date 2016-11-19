package com.example.administrator.email;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by Administrator on 2016/11/19 0019.
 */

public class content extends AppCompatActivity {
    private static DataInputStream input;
    private static DataOutputStream out;
    private TextView tv;
    private int id=Recievemail.id;
    private Thread thread;
    String string;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);
        input=Recievemail.input;
        out=Recievemail.out;
        tv= (TextView) findViewById(R.id.textView);
        content();
        while(thread.isAlive());
        tv.setText(string);
    }
    private void content(){
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    string="";
                    out.writeBytes("retr " + id + "\r\n");
                    //System.out.println("以下为第" + i + "封邮件的内容");
                    // int j=1;
                    while (true) {
                        String reply = input.readLine();
                        // System.out.println((j++)+reply);
                        string+=reply;
                        if (reply.toLowerCase().equals(".")) {
                            break;
                        }
                    }
                }catch (Exception e){
                    Log.i("logcat",e.toString());
                }
            }
        });
        thread.start();
    }
}
