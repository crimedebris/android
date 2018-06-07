package com.example.phenix.intent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button CON;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    boolean returnedData = data.getBooleanExtra("bool",false);
                    String booleans=String.valueOf(returnedData);
                    Toast.makeText(MainActivity.this,booleans,Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CON=(Button)findViewById(R.id.confirm);

        CON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data="hello";
                Intent intent=new Intent(MainActivity.this,Return.class);
                intent.putExtra("mes",data);
                startActivityForResult(intent,1);
            }
        });
    }
}
