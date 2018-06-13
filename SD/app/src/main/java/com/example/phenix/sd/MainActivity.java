package com.example.phenix.sd;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    Button CON;
    Button reads;
    EditText texts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CON=(Button)findViewById(R.id.confirm);
        texts=(EditText)findViewById(R.id.info);
        reads=(Button)findViewById(R.id.read);
        reads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileInputStream in=null;
                BufferedReader reader=null;
                StringBuilder content=new StringBuilder();
                try{
                    in=openFileInput("data");
                    reader=new BufferedReader(new InputStreamReader(in));
                    String line="";
                    while((line=reader.readLine())!=null)
                    {
                        content.append(line);
                        texts.setText(line);
                    }
                }catch (IOException e)
                {
                    e.printStackTrace();
                }finally {
                    if (reader!=null)
                    {
                        try{
                            reader.close();

                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
        CON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stexts=texts.getText().toString();
                if(!stexts.equals(""))
                {
                    FileOutputStream out=null;
                    BufferedWriter writer=null;
                    try
                    {
                        out=openFileOutput("data", Context.MODE_APPEND);
                        writer=new BufferedWriter(new OutputStreamWriter(out));
                        writer.write(stexts);
                    }catch (IOException e)
                       {
                             e.printStackTrace();
                        }
                        finally {
                             try{
                                if(writer!=null)
                                        writer.close();
                                 }catch (IOException e){
                            e.printStackTrace();
                                                        }
                    }
                    }
                }


        });
    }}

