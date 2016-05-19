package nomura_pro.airis;

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
 * Created by ne250214 on 16/05/03.
 */
public class RoomIdGetThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;
    int m_interlocutor_id;
    int m_server_news_id;
    String screen;
    String name;

    public RoomIdGetThread(Activity activity,SharedPreferences pref,int interlocutor_id,int server_news_id) {
        this.m_Activity = activity;
        this.m_pref = pref;
        this.m_interlocutor_id = interlocutor_id;
        this.m_server_news_id = server_news_id;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        screen = params[0];
        name = params[1];

        String message;

        //parsonal_login(ユーザ識別番号、セッションid、検索id)
        message = SocketConnect.connect("group parsonal_login " + m_pref.getString("id", "null") +" "+ m_pref.getString("session", "null")+" "+screen);

        return message;
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {
        //accept セッションid 部屋番号

        String[] my_user_data = param.split(" ", 0);

        if (my_user_data[0].equals("accept")){

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            MySQLiteOpenHelper helper = new MySQLiteOpenHelper(m_Activity);
            final SQLiteDatabase db = helper.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put("room_id", my_user_data[2]);
            values.put("type", 0);
            db.insert("group_table", null, values);

            Cursor c = db.query("group_table", new String[]{"_id", "room_id","name", "type"},
                    "room_id=?", new String[]{my_user_data[2]}, null, null, "name DESC");

            c.moveToFirst();
            int group_id = c.getInt(0);

            values = new ContentValues();
            values.put("group_id", group_id);
            db.update("friend_table", values, "screen_name=?", new String[]{screen});

            SendNewsThread thread = new SendNewsThread(m_Activity, m_pref);
            thread.execute(my_user_data[2],String.valueOf(m_server_news_id));
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