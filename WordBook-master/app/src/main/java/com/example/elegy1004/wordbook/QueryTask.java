package com.example.elegy1004.wordbook;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class QueryTask extends AsyncTask<String, Void, String> {
    Context context;
    TextView tv_result,eng;

    private static final String JUHE_URL_ENVIRONMENT_AIR_PM =
            "http://web.juhe.cn:8080/environment/air/pm";
    //private static final String JUHE_APPKEY = "07ff4d9b309fc689c948f994b8c46310";

    private static final String YOUDAO_KEY = "1303333811";
    private static final String YOUDAO_KEYFROM = "wordbookaaaa";

    public QueryTask(Context context, TextView tv_result,TextView eng) {
        // TODO Auto-generated constructor stub
        super();
        this.context = context;
        this.tv_result = tv_result;
        this.eng = eng;
    }



    @Override
    protected String doInBackground(String... params) {
        String city = params[0];

        ArrayList<NameValuePair> headerList = new ArrayList<NameValuePair>();
        headerList.add(new BasicNameValuePair("Content-Type", "text/html; charset=utf-8"));

        String targetUrl = "http://fanyi.youdao.com/openapi.do?keyfrom=wordbookaaaa&key=1303333811&type=data&doctype=json&version=1.1&q=" + city;


        HttpGet httpRequest = new HttpGet(targetUrl);
        try {
            for (int i = 0; i < headerList.size(); i++) {
                httpRequest.addHeader(headerList.get(i).getName(),
                        headerList.get(i).getValue());
            }

            HttpClient httpClient = new DefaultHttpClient();

            HttpResponse httpResponse = httpClient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                return strResult;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {

            try {
                JSONObject jsonObject = new JSONObject(result);
                String errorCode = jsonObject.getString("errorCode");

                if (errorCode.equals("20")) {
                    tv_result.setText("要翻译的文本过长");
                } else if (errorCode.equals("30 ")) {
                    tv_result.setText("无法进行有效的翻译");
                } else if (errorCode.equals("40")) {
                    tv_result.setText("不支持的语言类型");
                } else if (errorCode.equals("50")) {
                    tv_result.setText("无效的key");
                } else if (errorCode.equals("0")) {

                    //要翻译的内容
                    String query = jsonObject.getString("query");
                    eng.setText(query);

                    //翻译内容
                    JSONArray jsonArray = jsonObject.getJSONArray("translation");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        tv_result.setText(jsonArray.getString(i));
                    }
                    tv_result.setText(tv_result.getText() + "\n ");

                    if (jsonObject.has("basic")) {
                        JSONObject basic = jsonObject
                                .getJSONObject("basic");
                        if (basic.has("phonetic")) {
                            String phonetic = basic.getString("phonetic");
                            tv_result.setText(tv_result.getText() + "\n" + phonetic);

                        }
                        if (basic.has("explains")) {
                            String explains = basic.getString("explains");
                            tv_result.setText(tv_result.getText() + "\n" + explains);
                        }
                    }

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "查询失败",
                    Toast.LENGTH_LONG).show();
            tv_result.setText("");
        }
    }
}
