package co.martinbrown.example.tweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class ManhattanWeatherActivity extends Activity {

    TextView mTextWeather;
    Button mButtonWeather;
    WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTextWeather = (TextView) findViewById(R.id.textWeather);
        mButtonWeather = (Button) findViewById(R.id.buttonGetWeather);

        mButtonWeather.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getWeather();
            }
        });
    }

    public void getWeather() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mTextWeather.setText("Waiting for weather ...");
                        }
                    });
                    executeHttpGet();

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mTextWeather.setText("Error, couldn't get weather ...");
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void getAverageWeather(String raw) throws Exception {

        JSONObject response = new JSONObject(raw);
        JSONArray results = response.getJSONArray("results");

        int sum = 0;
        int count = 0;
        String text;
        int temp;

        for(int i = 0; i < results.length(); i++) {
            text = results.getJSONObject(i).getString("text");

            if(text != null) {

                StringTokenizer st = new StringTokenizer(text, " ");
                String degrees = null;
                String keyword = null;

                while(st.hasMoreTokens()) {
                    degrees = keyword;
                    keyword = st.nextToken();

                    if(keyword.equals("degrees"))
                        break;
                }

                if(degrees != null && keyword != null && keyword.equals("degrees")) {

                    try {
                        temp = Integer.parseInt(degrees);

                        if(temp >= 140)
                            continue;

                        sum += temp;
                    }
                    catch(NumberFormatException e) {
                        continue;
                    }

                    count++;
                }
            }
        }

        final int average = sum / count;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mTextWeather.setText(average + " degrees");
            }

        });
    }

    public void executeHttpGet() throws Exception {
        BufferedReader in = null;

        try {

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet("http://search.twitter.com/search.json?q=degrees&geocode=40.7834345,-73.9662495,11mi&result_type=recent");
            HttpResponse response = client.execute(request);

            final int statusCode = response.getStatusLine().getStatusCode();

            switch(statusCode) {

                case 200:

                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";
                    String NL = System.getProperty("line.separator");

                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }

                    in.close();

                    getAverageWeather(sb.toString());

                    break;

            }

        }
        finally {

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}