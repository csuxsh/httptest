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

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements OnClickListener {

    private String id = "00001828";
    private String psw = "shao0013";
    private String url = "http://auth.gionee.com/login?service=http%3A%2F%2Fhr.gionee.com%2F";
    private String captchaUrl = "https://eapply.abchina.com/coin/Helper/ValidCode.ashx";
    private DefaultHttpClient mClient = new DefaultHttpClient();//http客户端
    Button button;
    ImageView iv ;
    Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)this.findViewById(R.id.button);
        button.setOnClickListener(this);
        iv = (ImageView)this.findViewById(R.id.iamge);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                iv.setImageBitmap((Bitmap)msg.obj);
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
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                captcha();
            }
        }).start();
        */Toast.makeText(this,Build.DEVICE,Toast.LENGTH_SHORT).show();
    }


    Bitmap captcha()  {
        HttpGet httpGet = new HttpGet("http://auth.gionee.com/captcha");
        //HttpGet httpGet = new HttpGet("http://www.baidu.com");

        /* 发送请求并获得响应对象 */
        InputStream is = null;
        try {
            HttpResponse mHttpResponse = mClient.execute(httpGet);
            HttpEntity mHttpEntity = mHttpResponse.getEntity();
            is = mHttpEntity.getContent();
            int lenth = is.available();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Message msg = Message. ();
            msg.obj = bitmap;
            Log.d("xush", "count is " + mHttpEntity.getContentLength());
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(is));

            String result = "";
            String line = "";

            while (null != (line = bufferedReader.readLine()))
            {
                result += line;
            }

            // 将结果打印出来，可以在LogCat查看
           Log.d("xush", result);
            mHandler.sendMessage(msg);
            return bitmap;
        }catch(IOException e) {
            return null;
        }finally{
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void sendHttpRequset (){
        captcha();
        DefaultHttpClient client = new DefaultHttpClient();//http客户端
        HttpPost httpPost = new HttpPost(url);

        Map<String, String> parmas = new HashMap<String, String>();

        parmas.put("username", id);
        parmas.put("password", psw);
        parmas.put("j_captcha_response", "1");


    }
    /*
    String decode() {
        // 注意这里是普通会员账号，不是开发者账号，注册地址 http://www.yundama.com/index/reg/user
        // 开发者可以联系客服领取免费调试题分
        String username = "username";
        String password	= "password";

        // 测试时可直接使用默认的软件ID密钥，但要享受开发者分成必须使用自己的软件ID和密钥
        // 1. http://www.yundama.com/index/reg/developer 注册开发者账号
        // 2. http://www.yundama.com/developer/myapp 添加新软件
        // 3. 使用添加的软件ID和密钥进行开发，享受丰厚分成
        int 	appid	= 1;
        String 	appkey	= "22cc5376925e9387a23cf797cb9ba745";

        // 图片路径
        String	imagepath	= "img\\test.png";

        //  例：1004表示4位字母数字，不同类型收费不同。请准确填写，否则影响识别率。在此查询所有类型 http://www.yundama.com/price.html
        int codetype = 1004;

        // 只需要在初始的时候登陆一次
        int uid = 0;
        YDM.INSTANCE.YDM_SetAppInfo(appid, appkey);			// 设置软件ID和密钥
        uid = YDM.INSTANCE.YDM_Login(username, password);	// 登陆到云打码

        if(uid > 0){
            System.out.println("登陆成功,正在提交识别...");

            byte[] byteResult = new byte[30];
            int cid = YDM.INSTANCE.YDM_DecodeByPath(imagepath, codetype, byteResult);
            String strResult = new String(byteResult, "UTF-8").trim();

            // 返回其他错误代码请查询 http://www.yundama.com/apidoc/YDM_ErrorCode.html
            System.out.println("识别返回代码:" + cid);
            System.out.println("识别返回结果:" + strResult);

        }else{
            System.out.println("登录失败，错误代码为：" + uid);
        }
        */
    }
