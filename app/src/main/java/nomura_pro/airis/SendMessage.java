package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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

        pref = getSharedPreferences("my_profile", MODE_PRIVATE);

        //ここでトーク相手の情報をを受け取る
        Intent i = getIntent();
        interlocutor_group = i.getIntExtra("group_id", 0);
        interlocutor_name = i.getStringExtra("name");
        interlocutor_screen = i.getStringExtra("screen");
        setTitle(interlocutor_name);

        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c = db.query("group_table", new String[]{"room_id"},
                "_id=?", new String[]{String.valueOf(interlocutor_group)}, null, null, "name DESC");

        c.moveToFirst();
        room_id = c.getString(0);

        c.close();

        setTalk();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動

                String text = textET.getText().toString();
                text = text.replaceAll("\n", CommonUtilities.LINE_BREAK_DODE).replaceAll(" ", CommonUtilities.BLANK_CODE);
                if (text.equals(""))
                    Toast.makeText(SendMessage.this, "メッセージを入力してください", Toast.LENGTH_LONG).show();
                else {
                    SendMessageThread thread = new SendMessageThread(SendMessage.this, pref, layout, sl, textET,interlocutor_group,room_id,text);
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

        while (isEof) {

            sender_id = c.getInt(0);
            type = c.getInt(1);

            try {
                if (type == 0) {
                    text = new String(c.getBlob(2), "UTF-8");
                    text = text.replaceAll(CommonUtilities.LINE_BREAK_DODE, "\n").replaceAll(CommonUtilities.BLANK_CODE, " ");

                    if (sender_id == 0) {
                        setMessageYouTo(text);
                    } else {
                        setMessageToYou(text);
                    }
                } else {
                    news_id = ByteBuffer.wrap(c.getBlob(2)).getInt();
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

    public static void catchMessage(String name, String messe) {
        if (name.equals(interlocutor_name)) {
            setMessageToYou(messe);
        }
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
                NewsGetThread ngt = new NewsGetThread(this, pref, interlocutor_group, room_id);
                ngt.execute();
                break;
        }
        return true;
    }
}