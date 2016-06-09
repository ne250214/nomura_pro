package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ne250214 on 16/05/03.
 */
public class NewsSelectRoom  extends Activity {

    SharedPreferences pref;
    LinearLayout layout;
    View v;

    SQLiteDatabase db;

    int server_news_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        server_news_id = i.getIntExtra("server_news_id", 0);

        setContentView(R.layout.friend_list);
        layout = (LinearLayout) findViewById(R.id.friend_list_linerlayout);

    }

    public void setDB() {

        Cursor c = db.query("friend_table", new String[]{"_id", "name", "profile", "screen_name", "group_id"},
                "type=?", new String[]{"3"}, null, null, "name DESC");

        boolean isEof = c.moveToFirst();
        while (isEof) {
            final int id = c.getInt(0);
            final String name = c.getString(1).replaceAll(CommonUtilities.BLANK_CODE, " ");
            final String profile = c.getString(2).replaceAll(CommonUtilities.BLANK_CODE, " ");
            final String screen = c.getString(3);
            final int group_id = c.getInt(4);

            TextView tv = new TextView(getApplicationContext());

            final TextView nameTV = new TextView(getApplicationContext());
            nameTV.setText(name);
            nameTV.setTextSize(20);
            nameTV.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            nameTV.setTextColor(Color.BLACK);

            tv.setText(String.format("%s", profile));

            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendNews(name, screen, id, group_id);
                }
            });
            nameTV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendNews(name, screen, id, group_id);
                }
            });

            isEof = c.moveToNext();

            layout.addView(nameTV);
            layout.addView(tv);

            TextView line = new TextView(getApplicationContext());
            line.setBackgroundResource(R.drawable.line);

            layout.addView(line);
        }

        c.close();

    }

    void sendNews(String name, final String screen, int id, int group_id) {
        if (group_id == 0) {
            //トークルームを作っていなかったら
            SendNewsThreadNotRoomId sendNewsThreadNotRoomId = new SendNewsThreadNotRoomId(this,pref,id,server_news_id,screen,name);
            sendNewsThreadNotRoomId.execute();
        } else {
            //トークルームを作っていたら
            Cursor c = db.query("group_table", new String[]{"room_id"}, "_id=?",
                    new String[]{String.valueOf(group_id)}, null, null, null);

            c.moveToFirst();
            String room_id = c.getString(0);

            SendNewsThread thread = new SendNewsThread(this, pref,room_id,group_id,id);
            thread.execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        pref = getSharedPreferences("my_profile", MODE_PRIVATE);

        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(getApplicationContext());
        db = helper.getReadableDatabase();
        layout.removeAllViews();
        setDB();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.close();
    }
}

