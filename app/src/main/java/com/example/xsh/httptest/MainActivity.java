package com.example.xsh.httptest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.BatteryManager;
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
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.show.api.ShowApiRequest;


import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import Decoder.BASE64Encoder;


public class MainActivity extends Activity implements OnClickListener {


    private String mUrl = "https://eapply.abchina.com/coin/Coin/CoinSubmit?issueid=";//"http://auth.gionee.com/login?service=http%3A%2F%2Fhr.gionee.com%2F";
    private String mCaptchaUrl = "https://eapply.abchina.com/coin/Helper/ValidCode.ashx";
    private String mMmsCaptchaUrl = "https://eapply.abchina.com/coin/Coin/SendCaptchaCode";
    private String mTmpDir = "/sdcard/tmp_test/";
    private String mMmsNum = "1069095599";

    private DefaultHttpClient mClient = new DefaultHttpClient();//http客户端
    Button button;
    ImageView iv;
    Handler mHandler;
    WebView web;
    SmsContent content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(this);
        iv = (ImageView) this.findViewById(R.id.iamge);
        web = (WebView)this.findViewById(R.id.webView);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1)
                  iv.setImageBitmap((Bitmap) msg.obj);
                web.loadUrl("file:///sdcard/tmp_test/ret.html");
            }
        };
        /*
        while(true) {
            BatteryManager batteryManager = (BatteryManager)this.getSystemService(Context.BATTERY_SERVICE);
            Log.d("xush", ""+ batteryManager.getIntProperty(4));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */
        content = new SmsContent(MainActivity.this, new Handler());
        this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, content);
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
                sendHttpRequset();
            }
        }).start();

       // Toast.makeText(this, Build.DEVICE, Toast.LENGTH_SHORT).show();
    }

    // 识别验证码
    private String captcha() {
        HttpGet httpGet = new HttpGet(mCaptchaUrl);
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
            msg.what = 1;
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


    String getMmsCaptcha(String captcha) {
        HttpPost httpPost = new HttpPost(mMmsCaptchaUrl);
        Map<String, String> parmas = new HashMap<String, String>();
        parmas.put("mobile", "18620369547");
        parmas.put("piccode", captcha) ;
        String ret = sendPost(httpPost,parmas);


        return "1234";
    }

    String sendPost(HttpPost post, Map<String, String> parmas) {
        String returnConnection = null;

        ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();

        if(parmas != null){
            Set<String> keys = parmas.keySet();
            for(Iterator<String> i = keys.iterator(); i.hasNext();) {
                String key = (String)i.next();
                pairs.add(new BasicNameValuePair(key, parmas.get(key)));
            }
        }

        try {

            UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs, "utf-8");
            post.setEntity(p_entity);
            HttpResponse response = mClient.execute(post);
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            FileOutputStream  fos = new FileOutputStream(new File(mTmpDir + "ret.html"));
            if (convertStreamToHtml(fos, content)) {
                Message msg = Message.obtain();
                msg.what = 2;
                mHandler.sendMessage(msg);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnConnection;
    }

    void sendHttpRequset() {
        String captcha = captcha();
        String mmsCaptcha = getMmsCaptcha(captcha);
        synchronized (this) {
            try {
                // wait sms
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mmsCaptcha = content.sms;
        Log.d("xush", "sms is" + mmsCaptcha);
        //DefaultHttpClient client = new DefaultHttpClient();//http客户端
        HttpPost httpPost = new HttpPost(mUrl);

        Map<String, String> parmas = new HashMap<String, String>();

        parmas.put("name", "徐少华");
        parmas.put("cardtype", "0");
        parmas.put("identNo", "430682198803030515");
        parmas.put("mobile", "18620369547");
        parmas.put("piccode", captcha) ;
        parmas.put("phoneCaptchaNo", mmsCaptcha);
        parmas.put("orglevel1","深圳分行");
        parmas.put("coindat", "2016-10-01" );
        parmas.put("time", "amradio");

        String ret = sendPost(httpPost,parmas);

        Log.d("xush", "end" +  ret);
    }





    private String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        StringBuilder sb = new StringBuilder();

        String line = null;

        try {

            while ((line = reader.readLine()) != null) {

                sb.append(line);
                sb.append("\n");
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                is.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }

        return sb.toString();

    }

    public boolean convertStreamToHtml(FileOutputStream fos, InputStream is){

        byte[] buff = new byte[1024];
        int count = -1;
        Arrays.fill(buff, (byte)0);


        try {
            while ((count = (is.read(buff))) > 0) {
                fos.write(buff, 0, count);
            }
            return true;

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        } finally {

            try {

                is.close();
                fos.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }
    }

}