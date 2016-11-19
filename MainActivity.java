package com.example.administrator.email;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.net.Socket;

import sun.misc.BASE64Encoder;



public class MainActivity extends AppCompatActivity implements View.OnClickListener ,TextWatcher{

    private EditText emailAddress;//账号
    private EditText password;//密码
    private Button clearAddress;//清除账号
    private Button emailLogin;//登录
    private ProgressDialog dialog;
    private SharedPreferences sp;
    private CheckBox cb_remenber;//记住密码
    private CheckBox cb_autologin;//自动登录

    static OutputStream out=null;
    static BufferedReader reader=null;
    static Socket socket;
    static String user=null;
    static String pwd=null;

    static  String NAME_BASE64="";//用户名 base64编码
    static  String PASSWORD_BASE64="";//密码base64编码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp=getSharedPreferences("config", Context.MODE_APPEND);

        emailAddress=(EditText) findViewById(R.id.editText);
        password=(EditText) findViewById(R.id.editText2);

        clearAddress=(Button) findViewById(R.id.button2);
        emailLogin=(Button) findViewById(R.id.button);

        cb_remenber=(CheckBox) findViewById(R.id.checkBox);
        cb_autologin=(CheckBox) findViewById(R.id.checkBox2);

        clearAddress.setOnClickListener(this);
        emailAddress.addTextChangedListener(this);
        emailLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                socket = new Socket();
                encode();
                new Thread(new Runnable() {
                    public void run() {
                        if (!loginEmail()) {
                            return;
                        }
                    }
                }).start();
            }
        });

        cb_remenber.setOnClickListener(this);
        cb_autologin.setOnClickListener(this);

        isRemenberPwd();
    }

    /**
     * 是否记住密码
     */
    private void isRemenberPwd(){
        boolean isRbPwd=sp.getBoolean("isRbPwd", false);
        if(isRbPwd){
            String addr=sp.getString("address", "");
            String pwd=sp.getString("password", "");
            emailAddress.setText(addr);
            password.setText(pwd);
            cb_remenber.setChecked(true);
        }
    }

    /**
     * 记住密码
     */
    private void remenberPwd(){
        boolean isRbPwd=sp.getBoolean("isRbPwd", false);
        if(isRbPwd){
            sp.edit().putBoolean("isRbPwd", false).commit();
            cb_remenber.setChecked(false);
        }else{
            sp.edit().putBoolean("isRbPwd", true).commit();
            sp.edit().putString("address", emailAddress.getText().toString().trim()).commit();
            sp.edit().putString("password", password.getText().toString().trim()).commit();
            cb_remenber.setChecked(true);
        }
    }

    /**
     * 登入邮箱
     */
    private boolean loginEmail(){
        try{
            String line=null;
            if(!socket.isConnected()){
                socket.connect(new InetSocketAddress("smtp.163.com", 25), 3000);//链接服务器
                reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out=socket.getOutputStream();
                line=reader.readLine();
                Log.i("logcat", line);
                if(!line.contains("220")){
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "登录失败，未连接服务器", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    return false;
                }
            }
            out.write("helo 163.com\r\n".getBytes("utf-8"));//发送问候消息
            line=reader.readLine();
            Log.i("logcat", line);
            if(!line.contains("250")){
                Looper.prepare();
                Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                Looper.loop();                    return false;
            }

            out.write("auth login\r\n".getBytes("utf-8"));//请求登录
            line=reader.readLine();
            Log.i("logcat", line);
            if(!line.contains("334")){
                Looper.prepare();
                Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                return false;
            }
            out.write(NAME_BASE64.getBytes("utf-8"));//发送账户
            line=reader.readLine();
            Log.i("logcat", line);
            if(!line.contains("334")){
                Looper.prepare();
                Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                return false;
            }
            out.write(PASSWORD_BASE64.getBytes("utf-8"));//发送密码
            line=reader.readLine();
            Log.i("logcat", line);
            if(!line.contains("successful")){
                Looper.prepare();
                Toast.makeText(MainActivity.this, "登录失败，账户或密码错误", Toast.LENGTH_SHORT).show();
                Looper.loop();
                return false;
            }else{
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                Looper.prepare();
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                Looper.loop();
                out.write("QUIT\r\n".getBytes("UTF-8"));//退出登录
                line=reader.readLine();
                out.close();
                reader.close();
                socket.close();
            }
        }catch (Exception e){
            Looper.prepare();
            Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
            Looper.loop();
            return false;
        }

        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button2:
                emailAddress.setText("");
                break;
            case R.id.checkBox:
                remenberPwd();
                break;
            case R.id.checkBox2:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!TextUtils.isEmpty(s)){
            clearAddress.setVisibility(View.VISIBLE);
        }else{
            clearAddress.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void encode(){
        user=emailAddress.getText().toString();
        pwd=password.getText().toString();
        BASE64Encoder decode=new BASE64Encoder();
        try {
            NAME_BASE64= decode.encode(user.getBytes()).toString()+"\r\n";
            PASSWORD_BASE64=decode.encode(pwd.getBytes()).toString()+"\r\n";
        } catch (Exception e) {

        }
    }
}










