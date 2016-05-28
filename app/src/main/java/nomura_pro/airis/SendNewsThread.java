package nomura_pro.airis;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.widget.Toast;

import java.nio.ByteBuffer;

/**
 * Created by ne250214 on 16/04/27.
 */
public class SendNewsThread extends AsyncTask<String, Void, String> {

    Activity m_activity;
    SharedPreferences m_pref;

    String m_room_id;
    int m_group_id;
    int m_news_id;

    public SendNewsThread(Activity activity, SharedPreferences pref,String room_id,int group_id,int news_id) {
        this.m_activity = activity;
        this.m_pref = pref;
        this.m_room_id = room_id;
        this.m_group_id = group_id;
        this.m_news_id = news_id;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        //news(ユーザ識別番号、セッションid、グループコード、ニュースid)
        message = SocketConnect.connect("send news " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id + " " + m_news_id);

        return message;
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        String[] my_user_data = param.split(" ", 0);

        if (my_user_data[0].equals("accept")) {

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(m_activity);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            ContentValues values = new ContentValues();
            values.put("group_id", m_group_id);
            values.put("sender_id", 0);
            values.put("message", ByteBuffer.allocate(4).putInt(m_news_id).array());
            values.put("type", 1);

            db.insert("talk_table", null, values);

            m_activity.finish();


        } else if (my_user_data[0].equals("undefined_session_id")) {

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putBoolean("session_validness", false);
            editor.apply();

            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(m_activity);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("friend_table", "_id like '%'", null);
            db.delete("talk_table", "_id like '%'", null);

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.Logout");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            m_activity.startActivity(intent);
        } else if (ErrorMessages.ERROR_HASH_MAP.containsKey(my_user_data[0])) {
            Toast.makeText(m_activity, ErrorMessages.ERROR_HASH_MAP.get(my_user_data[0]), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(m_activity, my_user_data[0], Toast.LENGTH_LONG).show();
        }
    }

}