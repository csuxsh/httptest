package com.example.xsh.httptest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.show.api.ShowApiRequest;


import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import sun.misc.BASE64Encoder;


public class MainActivity extends Activity implements OnClickListener {

    private String id = "00001828";
    private String psw = "shao0013";
    private String url = "http://auth.gionee.com/login?service=http%3A%2F%2Fhr.gionee.com%2F";
    private String captchaUrl = "https://eapply.abchina.com/coin/Helper/ValidCode.ashx";
    private String mTmpDir = "/sdcard/tmp_test/";
    private DefaultHttpClient mClient = new DefaultHttpClient();//http客户端
    Button button;
    ImageView iv;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(this);
        iv = (ImageView) this.findViewById(R.id.iamge);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                iv.setImageBitmap((Bitmap) msg.obj);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
       new Thread(new Runnable() {
            @Override
            public void run() {
                captcha();
            }
        }).start();

       // Toast.makeText(this, Build.DEVICE, Toast.LENGTH_SHORT).show();
    }

    // 识别验证码
    String captcha() {
        HttpGet httpGet = new HttpGet(captchaUrl);
        //HttpGet httpGet = new HttpGet("http://www.baidu.com");

        /* 发送请求并获得响应对象 */
        InputStream is = null;
        FileOutputStream fos = null;
        byte[] data = null;
        try {
            HttpResponse mHttpResponse = mClient.execute(httpGet);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            is = mHttpEntity.getContent();
            fos = new FileOutputStream(String.format("%s%s", mTmpDir, Thread.currentThread()));
            int lenth = is.available();
            data = new byte[lenth];
            is.read(data);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            BASE64Encoder encoder = new BASE64Encoder();
            String code = encoder.encode(data);
            Message msg = Message.obtain();
            msg.obj = bitmap;
            // debug
            Log.d("xush", "count is " + mHttpEntity.getContentLength());
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(is));

            String result = "";
            String line = "";

            while (null != (line = bufferedReader.readLine())) {
                result += line;
            }
            // debug
            String appid="24624";//要替换成自己的
            String secret="adc5e7e41c4747bf9ce3278fd55b8510";//要替换成自己的
            final String res=new ShowApiRequest("http://route.showapi.com/184-1",appid,secret)
                    .addTextPara("sd",code)
                    //.addFilePara("sd", new File(String.format("%s%s", mTmpDir, Thread.currentThread())))
                    .addTextPara("typeId", "")
                    .addTextPara("convert_to_jpg", "")
                    .post();
            System.out.println(res);
            // 将结果打印出来，可以在LogCat查看
            Log.d("xush", result);
            Log.d("xush", res);
            mHandler.sendMessage(msg);
            return res;
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void sendHttpRequset() {
        captcha();
        DefaultHttpClient client = new DefaultHttpClient();//http客户端
        HttpPost httpPost = new HttpPost(url);

        Map<String, String> parmas = new HashMap<String, String>();

        parmas.put("username", id);
        parmas.put("password", psw);
        parmas.put("j_captcha_response", "1");


    }

}