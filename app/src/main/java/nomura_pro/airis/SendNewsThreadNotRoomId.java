package nomura_pro.airis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.widget.Toast;

/**
 * Created by ne250214 on 16/06/01.
 */
public class SendNewsThreadNotRoomId extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;
    int m_interlocutor_id;
    String m_screen;
    String m_name;

    String m_room_id;
    int m_group_id;
    int m_news_id;

    public SendNewsThreadNotRoomId(Activity activity,SharedPreferences pref,int interlocutor_id,int news_id,String screen,String name) {
        this.m_Activity = activity;
        this.m_pref = pref;
        this.m_interlocutor_id = interlocutor_id;
        this.m_news_id = news_id;
        this.m_screen = screen;
        this.m_name = name;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        //parsonal_login(ユーザ識別番号、セッションid、検索id)
        message = SocketConnect.connect("group parsonal_login " + m_pref.getString("id", "null") +" "+ m_pref.getString("session", "null")+" "+m_screen);

        String[] my_user_data = message.split(" ", 0);

        if (my_user_data[0].equals("accept")){

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            MySQLiteOpenHelper helper = new MySQLiteOpenHelper(m_Activity);
            final SQLiteDatabase db = helper.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put("room_id", my_user_data[2]);
            values.put("type", 0);
            values.put("last_updated", 0);
            db.insert("group_table", null, values);

            Cursor c = db.query("group_table", new String[]{"_id",},
                    "room_id=?", new String[]{my_user_data[2]}, null, null, null);

            c.moveToFirst();
            m_group_id = c.getInt(0);

            m_room_id = my_user_data[2];

            values = new ContentValues();
            values.put("group_id", m_group_id);
            db.update("friend_table", values, "screen_name=?", new String[]{m_screen});

            message = SocketConnect.connect("send news " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id + " " + m_news_id);

            my_user_data = message.split(" ", 0);

            if (my_user_data[0].equals("accept")) {

                editor = m_pref.edit();
                editor.putString("session", my_user_data[1]);
                editor.apply();
            }
        }
        return message;
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {
        //accept セッションid 部屋番号

        String[] my_user_data = param.split(" ", 0);

        if (my_user_data[0].equals("accept")){
            m_Activity.finish();
        }

        else if (my_user_data[0].equals("undefined_session_id")){

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putBoolean("session_validness", false);
            editor.apply();

            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(m_Activity);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("friend_table", "_id like '%'", null);
            db.delete("talk_table", "_id like '%'", null);

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.Logout");
            m_Activity.startActivity(intent);
        }
        else if(ErrorMessages.ERROR_HASH_MAP.containsKey(my_user_data[0])){
            Toast.makeText(m_Activity, ErrorMessages.ERROR_HASH_MAP.get(my_user_data[0]), Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(m_Activity, my_user_data[0], Toast.LENGTH_LONG).show();
        }
    }
}