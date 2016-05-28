package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by ne250214 on 15/07/10.
 */
public class SendMessage extends Activity {

    int interlocutor_group;
    static String interlocutor_name;
    String interlocutor_screen;
    String room_id;
    static Boolean brows;

    SharedPreferences pref;
    SQLiteDatabase db;

    ScrollView sl;
    static LinearLayout layout;

    Button sendBtn;

    EditText textET;

    static Activity send_message_act;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");
        setContentView(R.layout.talk);
        layout = (LinearLayout) findViewById(R.id.talk_linerlayout);
        sendBtn = (Button) findViewById(R.id.talk_send_btn);
        textET = (EditText) findViewById(R.id.talk_send_text);
        sl = (ScrollView) findViewById(R.id.talk_scrollview);
        send_message_act = this;

    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("onResume");

        pref = getSharedPreferences("my_profile", MODE_PRIVATE);
        brows = pref.getBoolean("browse_setting", true);

        //ここでトーク相手の情報をを受け取る
        Intent i = getIntent();
        interlocutor_group = i.getIntExtra("group_id", 0);
        interlocutor_name = i.getStringExtra("name");
        interlocutor_screen = i.getStringExtra("screen");
        room_id = i.getStringExtra("room_id");

        setTitle(interlocutor_name);

        setTalk();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動

                String text = textET.getText().toString();
                text = text.replaceAll("\n", CommonUtilities.LINE_BREAK_DODE).replaceAll(" ", CommonUtilities.BLANK_CODE);
                if (text.equals(""))
                    Toast.makeText(SendMessage.this, "メッセージを入力してください", Toast.LENGTH_LONG).show();
                else {
                    SendMessageThread thread = new SendMessageThread(SendMessage.this, pref, layout, sl, textET, interlocutor_group, room_id, text);
                    thread.execute(room_id, text);
                }

            }
        });
    }

    private void setTalk() {

        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        db = helper.getReadableDatabase();

        layout.removeAllViews();

        //トーク履歴を取り出すためのクエリ
        //type=0 message type=1 news
        Cursor c = db.query("talk_table", new String[]{"sender_id", "type", "message"},
                "group_id=?", new String[]{String.valueOf(interlocutor_group)}, null, null, "_id ASC");

        boolean isEof = c.moveToFirst();

        int sender_id;
        int type;
        String text;
        int news_id = 0;

        String title = null;
        int type_id;
        String body = null;
        String link_url = null;
        String create_at = null;
        String image_url;
        int width;
        int height;
        byte[] blob;

        while (isEof) {

            sender_id = c.getInt(0);
            type = c.getInt(1);

            try {
                if (type == 0) {
                    //テキストだったら
                    System.out.println("テキスト");

                    text = new String(c.getBlob(2), "UTF-8");
                    text = text.replaceAll(CommonUtilities.LINE_BREAK_DODE, "\n").replaceAll(CommonUtilities.BLANK_CODE, " ");

                    if (sender_id == 0) {
                        setMessageYouTo(text);
                    } else {
                        setMessageToYou(text);
                    }
                } else {
                    //ニュースだったら
                    System.out.println("ニュース");
                    news_id = ByteBuffer.wrap(c.getBlob(2)).getInt();
                    System.out.println(news_id);

                    Cursor news_c = db.query(false, "news_table",
                            new String[]{"title", "type_id", "body", "link_url", "create_at", "image_url", "width", "height", "image_byte"},
                            "_id=?", new String[]{String.valueOf(news_id)}, null, null, null, null);

                    news_c.moveToFirst();

                    title = news_c.getString(0);
                    type_id = news_c.getInt(1);
                    body = news_c.getString(2);
                    link_url = news_c.getString(3);
                    create_at = news_c.getString(4);
                    image_url = news_c.getString(5);
                    width = news_c.getInt(6);
                    height = news_c.getInt(7);
                    blob = news_c.getBlob(8);
                    if (sender_id == 0) {
                        System.out.println("sender_id == 0");
                        setNewsYouTo(title + " " + body + " " + create_at, link_url);
                    } else {
                        System.out.println("sender_id == 0 else");
                        setNewsToYou(title + " " + body + " " + create_at, link_url);
                    }
                    news_c.close();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            isEof = c.moveToNext();
        }
        c.close();
        sl.scrollTo(0, sl.getBottom());
    }

    public static void setMessageYouTo(String messe) {
        try {
            TextView tv = new TextView(send_message_act);
            LinearLayout ll = new LinearLayout(send_message_act);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //left,top,right,bottom
            ll.setGravity(Gravity.RIGHT);
            lp.setMargins(10, 10, 100, 10);
            tv.setLayoutParams(lp);

            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(R.drawable.corner_round_blue);
            tv.setText(messe);
            tv.setTextSize(20);

            ll.addView(tv);
            layout.addView(ll);

        } catch (java.lang.NullPointerException ignored) {
        }
    }

    public void setNewsYouTo(String messe, final String link_url) {
        try {
            TextView tv = new TextView(send_message_act);
            LinearLayout ll = new LinearLayout(send_message_act);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //left,top,right,bottom
            ll.setGravity(Gravity.RIGHT);
            lp.setMargins(10, 10, 100, 10);
            tv.setLayoutParams(lp);

            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(R.drawable.corner_round_blue);
            tv.setText(messe);
            tv.setTextSize(20);

            if (brows) {
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        openLink(link_url);
                    }
                });
            } else {
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        openLink_brows(link_url);
                    }
                });
            }

            ll.addView(tv);
            layout.addView(ll);

        } catch (java.lang.NullPointerException ignored) {
        }
    }

    public static void setMessageToYou(String messe) {
        try {
            TextView tv = new TextView(send_message_act);
            LinearLayout ll = new LinearLayout(send_message_act);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            ll.setGravity(Gravity.LEFT);
            lp.setMargins(100, 10, 10, 10);
            tv.setLayoutParams(lp);

            tv.setBackgroundResource(R.drawable.corner_round);
            tv.setText(messe);
            tv.setTextSize(20);

            ll.addView(tv);
            layout.addView(ll);
        } catch (java.lang.NullPointerException ignored) {
        }
    }

    public void setNewsToYou(String messe, final String link_url) {
        try {
            TextView tv = new TextView(send_message_act);
            LinearLayout ll = new LinearLayout(send_message_act);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            ll.setGravity(Gravity.LEFT);
            lp.setMargins(100, 10, 10, 10);
            tv.setLayoutParams(lp);

            tv.setBackgroundResource(R.drawable.corner_round);
            tv.setText(messe);
            tv.setTextSize(20);


            if (brows) {
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        openLink(link_url);
                    }
                });
            } else {
                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        openLink_brows(link_url);
                    }
                });
            }


            ll.addView(tv);
            layout.addView(ll);
        } catch (java.lang.NullPointerException ignored) {
        }
    }

    public static void catchMessage(String name, String messe) {
        if (name.equals(interlocutor_name)) {
            setMessageToYou(messe);
        }
    }

    private void openLink(String link_url) {
        Intent intent = new Intent();
        intent.setClassName("nomura_pro.airis", "nomura_pro.airis.MyWebView");
        intent.putExtra("url", link_url);
        startActivity(intent);
    }

    private void openLink_brows(String link_url) {
        Uri uri = Uri.parse(link_url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    @Override
    public void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムの追加
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.talk_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.talk_menu_block:
                FriendBlockThread fbt = new FriendBlockThread(this, pref);
                fbt.execute(interlocutor_screen);
                break;
            case R.id.talk_menu_open_news:
                NewsListThread nlt = new NewsListThread(this, pref, interlocutor_group, room_id);
                nlt.execute();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        // TODO 自動生成されたメソッド・スタブ
        super.onDestroy();
        System.out.println("onDestrouy");
    }

    @Override
    protected void onRestart() {
        // TODO 自動生成されたメソッド・スタブ
        super.onRestart();
        System.out.println("onRestart");
    }

    @Override
    protected void onStart() {
        // TODO 自動生成されたメソッド・スタブ
        super.onStart();
        System.out.println("onStart");
    }

    @Override
    protected void onStop() {
        // TODO 自動生成されたメソッド・スタブ
        super.onStop();
        System.out.println("onStop");
    }
}