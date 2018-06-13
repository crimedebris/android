package com.example.phenix.hander;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity{
    public static final int UPDATE_TEXT=1;
    private TextView text;
    Button executes;
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_TEXT:
                  text.setText("update");
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executes=(Button)findViewById(R.id.execute);
        text=(TextView) findViewById(R.id.text);
        executes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               switch(v.getId()){
                  case R.id.execute:
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                             Message message=new Message();
                             message.what=UPDATE_TEXT;
                             handler.sendMessage(message);

                     }
                 }).start();
                      break;
                  default:
                      break;
               }
            }
        });

    }
}
