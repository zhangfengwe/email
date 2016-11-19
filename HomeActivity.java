package com.example.administrator.email;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Administrator on 2016/11/16 0016.
 */

public class HomeActivity extends AppCompatActivity {

    private Button bt3;//发送
    private Button bt4;//添加附件
    private Button bt5;//收件箱

    private EditText et3;//发件人
    private EditText et4;//收件人
    private EditText et5;//主题
    private EditText et6;//正文

    private String password;

    static OutputStream out=null;
    static BufferedReader reader=null;
    static private String FROM="mail from:";//用户名
    static private String TO="rcpt to:";//收件人.
    static private String SUBJECT="";
    static private String from="";
    String line="";
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emailhome);

        out=MainActivity.out;
        reader=MainActivity.reader;
        socket=MainActivity.socket;

        bt3= (Button) findViewById(R.id.button3);
        bt4= (Button) findViewById(R.id.button4);
        bt5= (Button) findViewById(R.id.button5);

        et3= (EditText) findViewById(R.id.editText3);
        et4= (EditText) findViewById(R.id.editText4);
        et5= (EditText) findViewById(R.id.editText5);
        et6= (EditText) findViewById(R.id.editText6);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
       //et3.setText(bundle.getString("username"));
//        password=bundle.getString("password");

        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initial();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send();
                    }
                }).start();
            }
        });

        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,Recievemail.class);
                startActivity(intent);
            }
        });
    }

    private boolean send(){
        try {
            reLogin();
            out.write(FROM.getBytes("utf-8"));//发件人
            line=reader.readLine();
            Log.i("logcat", "from:" + line);
            if(!line.contains("250")){
                from="";
                Looper.prepare();
                Toast.makeText(HomeActivity.this, "发送失败，发件人错误", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            out.write(TO.getBytes("utf-8"));//收件人
            line=reader.readLine();
            Log.i("logcat", "to:" + line);
            if(!line.contains("250")){
                from="";
                Looper.prepare();
                Toast.makeText(HomeActivity.this, "发送失败，收件人错误", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            out.write("data\r\n".getBytes("utf-8"));
            line=reader.readLine();
            Log.i("logcat", line);
            if(!line.contains("354")){
                from="";
                Looper.prepare();
                Toast.makeText(HomeActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
                return false;
            }
            out.write(("From:" + from+ "\r\n"
                    + "To:"+((EditText) findViewById(R.id.editText3)).getText().toString()+"\r\n"
                    + "Subject:"+SUBJECT).getBytes("UTF-8"));

            out.write(("\r\t" + et6.getText().toString()).getBytes("UTF-8"));

////            ou.write(("From:13083681098@163.com\r\n"//发件人，要和前面的一致
//            + "To:cloudlou@163.com\r\n" //收件人，要和前面的一致
//                    + "Subject:"+subject+"\r\n\r\n").getBytes("UTF-8"));//邮件主题
//
//            ou.write(("\r\t"+content).getBytes("UTF-8"));//邮件正文内容
            out.write("\r\n.\r\n".getBytes("UTF-8"));
            line=reader.readLine();
            Log.i("logcat", line);
            if(line.contains("250")){
                from="";
                Looper.prepare();
                Toast.makeText(HomeActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }else{
                from="";
                Looper.prepare();
                Toast.makeText(HomeActivity.this, "发送失败，垃圾邮件", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            out.write("QUIT\r\n".getBytes("UTF-8"));//退出登录
            line=reader.readLine();
            out.close();
            reader.close();
            socket.close();
            return true;
        }catch (Exception e) {
            from="";
            Looper.prepare();
            Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            Looper.loop();
            return false;
        }
    }
    private void reLogin(){
        try{
            socket=new Socket();
            socket.connect(new InetSocketAddress("smtp.163.com", 25), 3000);//链接服务器
            reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=socket.getOutputStream();
            reader.readLine();
            out.write("helo 163.com\r\n".getBytes("utf-8"));//发送问候消息
            reader.readLine();
            out.write("auth login\r\n".getBytes("utf-8"));//请求登录
            reader.readLine();
            out.write(MainActivity.NAME_BASE64.getBytes("utf-8"));//发送账户
            reader.readLine();
            out.write(MainActivity.PASSWORD_BASE64.getBytes("utf-8"));//发送密码
            reader.readLine();
        }catch (Exception e){
            from="";
            Looper.prepare();
            Toast.makeText(HomeActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    private void initial(){
        from=et3.getText().toString();
        if(!from.contains(MainActivity.user)){
            from+="<"+MainActivity.user+"@163.com>";
        }
        FROM="mail from:<"+MainActivity.user+">\r\n";
        TO="rcpt to:<"+et4.getText()+">\r\n";
        SUBJECT=et5.getText().toString()+"\r\n\r\n";
    }
}




