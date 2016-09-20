package com.example.xsh.httptest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Decoder.BASE64Encoder;


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
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
               // captcha();
            }
        }).start();

       // Toast.makeText(this, Build.DEVICE, Toast.LENGTH_SHORT).show();
    }

    // 识别验证码
    private String captcha() {
        HttpGet httpGet = new HttpGet(captchaUrl);
        /* 发送请求并获得响应对象 */
        InputStream is = null;
        ByteArrayOutputStream output =  null;
        byte[] data = null;
        String ret = null;

        try {
            HttpResponse mHttpResponse = mClient.execute(httpGet);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            is = mHttpEntity.getContent();

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap == null) {
                Log.e("xush", "图片获取失败");
                return null;
            }
            output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            data = output.toByteArray();

            String code = Base64.encodeToString(data, Base64.NO_WRAP);

            Message msg = Message.obtain();
            byte[] data2 = Base64.decode(code, Base64.NO_WRAP);
            msg.obj = bitmap;

            // debug
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(is));

            String result = "";
            String line = "";

            while (null != (line = bufferedReader.readLine())) {
                result += line;
            }
            // debug

            // 识别验证码
            String appid="24624";//要替换成自己的
            String secret="adc5e7e41c4747bf9ce3278fd55b8510";//要替换成自己的
            final String res=new ShowApiRequest("http://route.showapi.com/184-2",appid,secret)
                    .addTextPara("img_base64",code)
                    //.addFilePara("sd", new File(String.format("%s%s", mTmpDir, Thread.currentThread())))
                    .addTextPara("typeId", "3040")
                    .addTextPara("convert_to_jpg", "1")
                    .post();


            JSONTokener jasonCaptcha = new JSONTokener(res);
            try {
                JSONObject captcha = (JSONObject) jasonCaptcha.nextValue();
                ret = captcha.getJSONObject("showapi_res_body").getString("Result");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("xush", "jason 解析出错");
            }
            Log.e("xush", "captcha is "  + ret);
            mHandler.sendMessage(msg);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                }catch (IOException e2) {
                    e2.printStackTrace();
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