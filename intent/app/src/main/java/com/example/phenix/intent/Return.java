package com.example.phenix.intent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Return extends AppCompatActivity {
    Button RETURN;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return);
        Intent intent=getIntent();
        String message=intent.getStringExtra("mes");
        Toast.makeText(Return.this,message,Toast.LENGTH_SHORT).show();
        RETURN=(Button)findViewById(R.id.RE);
        RETURN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             Intent intent=new Intent();
                intent.putExtra("bool",true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
