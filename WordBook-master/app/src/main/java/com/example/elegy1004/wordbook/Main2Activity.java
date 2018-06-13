package com.example.elegy1004.wordbook;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {
    private CharSequence addedText;
    private WebView webView;
    MenuItem.OnMenuItemClickListener handler;
    private ActionMode mActionMode = null;
    String TAG = Main2Activity.class.getName();
    ActionMode.Callback mCallback=new ActionMode.Callback(){

        /**
         * 创建菜单的样式，返回true说明创建成功
         * @param actionMode
         * @param menu
         * @return
         */
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater menuInflater = actionMode.getMenuInflater();
            menuInflater.inflate(R.menu.action_mode,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        /**
         * 当ActionMode的条目被点击的时候，调用这个方法
         * @param actionMode
         * @param menuItem
         * @return
         */
        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        /**
         * 当ActionMode被销毁的时候调用
         * @param actionMode
         */
        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            if(actionMode!=null){
                actionMode.finish();
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        webView = (WebView) findViewById(R.id.toutput);
        registerClipEvents();


        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url); //根据传入的参数再去加载新的网页
                return true;//表示当前WebView可以处理打开新网页的请求，不用借助系统浏览器
            }

        });
        webView.loadUrl("http://m.enread.com/index.php");


    }



    private void registerClipEvents() {

        final ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        manager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {

                if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {

                 addedText = manager.getPrimaryClip().getItemAt(0).getText();

                    if (addedText != null) {
                        Intent intent=new Intent(Main2Activity.this,MainActivity4.class);

                        intent.putExtra("get_English",addedText.toString());
                        Main2Activity.this.startActivity(intent);
                        Toast.makeText(Main2Activity.this,addedText ,Toast.LENGTH_SHORT).show();
                    }
                  }
            }


        });
    }
}