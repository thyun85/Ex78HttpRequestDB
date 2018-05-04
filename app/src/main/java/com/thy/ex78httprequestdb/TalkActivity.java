package com.thy.ex78httprequestdb;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class TalkActivity extends AppCompatActivity {

    ListView listView;
    TalkAdapter talkAdapter;

    ArrayList<TalkItem> talkItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

//        //테스트용
//        talkItems.add(new TalkItem(1, "aaa", "hello", "https://i.pinimg.com/originals/cd/55/09/cd5509ff008a513abf7c3946c47bf744.jpg", "2018-04-18"));

        //데이터를 서버에서 읽어오기
        loadDB();

        listView = findViewById(R.id.listview);
        talkAdapter = new TalkAdapter(getLayoutInflater(), talkItems);
        listView.setAdapter(talkAdapter);

    }

    public void loadDB(){

        //Volley library 사용 가능

        new Thread(){
            @Override
            public void run() {

                String serverUrl = "http://thyun85.dothome.co.kr/android/loadDB.php";

                try {
                    URL url = new URL(serverUrl);

                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setUseCaches(false);

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String line = reader.readLine();

                    while(true){
                        buffer.append(line);

                        line = reader.readLine();
                        if(line == null) break;

                        buffer.append("\n");
                    }

                    //읽어온 데이터 문자열에서 db의 row(레코드)별로 배열로 분리하기
                    String[] rows = buffer.toString().split(";");

                    talkItems.clear();

                    for(String row : rows){
                        String[] datas = row.split("&");
                        if(datas.length != 5) continue;

                        int no = Integer.parseInt(datas[0]);
                        String name = datas[1];
                        String msg = datas[2];
                        final String imgPath = "http://thyun85.dothome.co.kr/android/"+datas[3];
                        String date = datas[4];

                        talkItems.add(0, new TalkItem(no, name, msg, imgPath, date));

                        //리스트뷰 갱신
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                talkAdapter.notifyDataSetChanged();
                                Toast.makeText(TalkActivity.this, imgPath, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}
