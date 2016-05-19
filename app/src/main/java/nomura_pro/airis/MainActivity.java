package nomura_pro.airis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * Created by ne250214 on 15/10/14.
 */
public class MainActivity extends FragmentActivity implements FragmentTabHost.OnTabChangeListener {

    Button button1;
    Button button2;
    Button button3;
    Button button4;

    LinearLayout layout1;
    LinearLayout layout2;
    LinearLayout layout3;
    LinearLayout layout4;

    TextView tv1;
    TextView tv2;
    TextView tv3;
    TextView tv4;

    int w = 100;
    int h = 100;

    int tabColor = Color.parseColor("#6bcff2");
    int textColor = Color.WHITE;
    FragmentTabHost tabHost;

    TabSpec tabSpec1, tabSpec2, tabSpec3, tabSpec4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FragmentTabHost を取得する
        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.container);

        layout1 = new LinearLayout(this);
        layout2 = new LinearLayout(this);
        layout3 = new LinearLayout(this);
        layout4 = new LinearLayout(this);

        button1 = new Button(this);
        button2 = new Button(this);
        button3 = new Button(this);
        button4 = new Button(this);

        tv1 = new TextView(this);
        tv2 = new TextView(this);
        tv3 = new TextView(this);
        tv4 = new TextView(this);

        setTitle("TALK");

        layout1.setGravity(Gravity.CENTER);
        layout1.setOrientation(LinearLayout.VERTICAL);
        layout1.setBackgroundColor(tabColor);
        tabSpec1 = tabHost.newTabSpec("TALK");
        button1.setBackgroundResource(R.drawable.talk_on);
        layout1.addView(button1, new LinearLayout.LayoutParams(w, h));
        tv1.setTextColor(textColor);
        tv1.setText("TALK");
        tv1.setGravity(Gravity.CENTER);
        layout1.addView(tv1);
        tabSpec1.setIndicator(layout1);
        tabHost.addTab(tabSpec1, FriendList.class, null);

        layout2.setGravity(Gravity.CENTER);
        layout2.setOrientation(LinearLayout.VERTICAL);
        layout2.setBackgroundColor(tabColor);
        tabSpec2 = tabHost.newTabSpec("NEWS");
        button2.setBackgroundResource(R.drawable.news_off);
        layout2.addView(button2, new LinearLayout.LayoutParams(w, h));
        tv2.setTextColor(textColor);
        tv2.setText("NEWS");
        tv2.setGravity(Gravity.CENTER);
        layout2.addView(tv2);
        tabSpec2.setIndicator(layout2);
        tabHost.addTab(tabSpec2, NewsListFragment.class, null);

        layout3.setGravity(Gravity.CENTER);
        layout3.setOrientation(LinearLayout.VERTICAL);
        layout3.setBackgroundColor(tabColor);
        tabSpec3 = tabHost.newTabSpec("FAVORITE");
        button3.setBackgroundResource(R.drawable.fav_off);
        layout3.addView(button3, new LinearLayout.LayoutParams(w, h));
        tv3.setTextColor(textColor);
        tv3.setGravity(Gravity.CENTER);
        tv3.setText("FAVORITE");
        layout3.addView(tv3);
        tabSpec3.setIndicator(layout3);
        tabHost.addTab(tabSpec3, FavoriteNews.class, null);

        layout4.setGravity(Gravity.CENTER);
        layout4.setOrientation(LinearLayout.VERTICAL);
        layout4.setBackgroundColor(tabColor);
        tabSpec4 = tabHost.newTabSpec("USER");
        button4.setBackgroundResource(R.drawable.user_off);
        layout4.addView(button4, new LinearLayout.LayoutParams(w, h));
        tv4.setTextColor(textColor);
        tv4.setGravity(Gravity.CENTER);
        tv4.setText("USER");
        layout4.addView(tv4);
        tabSpec4.setIndicator(layout4);
        tabHost.addTab(tabSpec4, MyProfileFragment.class, null);
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();

        button1.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        button1.setBackgroundResource(R.drawable.talk_on);
                        button2.setBackgroundResource(R.drawable.news_off);
                        button3.setBackgroundResource(R.drawable.fav_off);
                        button4.setBackgroundResource(R.drawable.user_off);
                        tabHost.setCurrentTab(0);
                    }
                }

        );

        button2.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        button1.setBackgroundResource(R.drawable.talk_off);
                        button3.setBackgroundResource(R.drawable.fav_off);
                        button4.setBackgroundResource(R.drawable.user_off);
                        button2.setBackgroundResource(R.drawable.news_on);
                        tabHost.setCurrentTab(1);
                    }
                }

        );

        button3.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        button1.setBackgroundResource(R.drawable.talk_off);
                        button2.setBackgroundResource(R.drawable.news_off);
                        button4.setBackgroundResource(R.drawable.user_off);
                        button3.setBackgroundResource(R.drawable.fav_on);
                        tabHost.setCurrentTab(2);
                    }
                }

        );

        button4.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        button1.setBackgroundResource(R.drawable.talk_off);
                        button2.setBackgroundResource(R.drawable.news_off);
                        button3.setBackgroundResource(R.drawable.fav_off);
                        button4.setBackgroundResource(R.drawable.user_on);
                        tabHost.setCurrentTab(3);
                    }
                }

        );

        // リスナー登録
        tabHost.setOnTabChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onTabChanged(String tabId) {
        Log.d("onTabChanged", "tabId: " + tabId);
        setTitle(tabId);
        switch (tabId) {
            case "TALK":
                button1.setBackgroundResource(R.drawable.talk_on);
                button2.setBackgroundResource(R.drawable.news_off);
                button3.setBackgroundResource(R.drawable.fav_off);
                button4.setBackgroundResource(R.drawable.user_off);
                break;
            case "NEWS":
                button1.setBackgroundResource(R.drawable.talk_off);
                button3.setBackgroundResource(R.drawable.fav_off);
                button4.setBackgroundResource(R.drawable.user_off);
                button2.setBackgroundResource(R.drawable.news_on);
                break;
            case "FAVORITE":
                button1.setBackgroundResource(R.drawable.talk_off);
                button2.setBackgroundResource(R.drawable.news_off);
                button4.setBackgroundResource(R.drawable.user_off);
                button3.setBackgroundResource(R.drawable.fav_on);
                break;
            case "USER":
                button1.setBackgroundResource(R.drawable.talk_off);
                button2.setBackgroundResource(R.drawable.news_off);
                button3.setBackgroundResource(R.drawable.fav_off);
                button4.setBackgroundResource(R.drawable.user_on);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {        // メニューアイテムの追加
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.top_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.top_menu_friend_add:
                intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.FriendSearchActivity");
                startActivity(intent);
                break;
            case R.id.top_menu_group_create:
                break;
            case R.id.top_menu_group_edit:
                break;
            case R.id.top_menu_group_list:
                break;
            case R.id.top_menu_group_block_list:
                intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.FriendBlockList");
                startActivity(intent);
                break;
        }
        return true;
    }
}