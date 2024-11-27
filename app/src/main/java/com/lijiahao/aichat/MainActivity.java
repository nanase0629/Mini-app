package com.lijiahao.aichat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者:李嘉豪
 * 学号:2123080112
 */
public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";

    private List<Msg> msgList = new ArrayList<>();
    private EditText input;
    private RecyclerView recyclerView;
    private LinearLayoutManager manager;
    private Button button;
    private MsgAdapter adapter;
    private String input_text;
    private StringBuilder response;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //获取解析数据，显示在Recycle中
            Bundle data = msg.getData();
            String result = data.getString("result");

            Msg msg_get = new Msg(result, Msg.MSG_RECEIVED);
            msgList.add(msg_get);

            //数据刷新
            adapter.notifyItemInserted(msgList.size() - 1);
            recyclerView.scrollToPosition(msgList.size() - 1);


        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initMsg();//初始化数据


        recyclerView = findViewById(R.id.recycle);
        button = findViewById(R.id.send);
        input = findViewById(R.id.input);

        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                input_text = input.getText().toString();
                Msg msg = new Msg(input_text, Msg.MSG_SEND);
                msgList.add(msg);

                adapter.notifyItemInserted(msgList.size() - 1);
                recyclerView.scrollToPosition(msgList.size() - 1);
                input.setText("");

                getInter();   //发起网络请求

            }

        });

    }


    private void getInter() {
        //开起线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(RobotManager.getUrl(input_text));
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);

                    InputStream in = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // 2,解析获得的数据
                    Gson gson = new Gson();
                    Msg msg = gson.fromJson(response.toString(), Msg.class);
                    Log.d(TAG, "result:" + msg.getType());
                    Log.d(TAG, "content:" + msg.getContent());


                    // 3，将解析的数据保存到 Message中，传递到主线程中显示
                    Bundle data = new Bundle();
                    Message msg1 = new Message();
                    if (msg.getType() == 0) {
                        data.putString("result", msg.getContent());
                    } else {
                        data.putString("result", "我不知道你在说什么！");
                    }
                    msg1.setData(data);
                    msg1.what = 1;
                    handler.sendMessage(msg1);


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }


        }).start();
    }


    private void initMsg() {
        Msg msg = new Msg("我是人工智能，快来和我聊天吧！", Msg.MSG_RECEIVED);
        msgList.add(msg);
    }
}
