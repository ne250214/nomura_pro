package nomura_pro.airis;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * Created by ne250214 on 15/10/13.
 */

public class SendMessageThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;
    LinearLayout m_layout;
    EditText m_textET;
    ScrollView m_sl;

    int m_group;
    String m_room_id;
    String m_text;

    public SendMessageThread(Activity activity, SharedPreferences pref, LinearLayout layout, ScrollView sl, EditText textET,int group_id,String room_id,String text) {
        this.m_Activity = activity;
        this.m_pref = pref;
        this.m_layout = layout;
        this.m_textET = textET;
        this.m_sl = sl;
        this.m_group = group_id;
        m_room_id = room_id;
        m_text = text;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        //message(ユーザ識別番号、セッションid、グループコード、メッセージ)
        message = SocketConnect.connect("send message " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id + " " + m_text);

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

            m_textET.setText("");

            MySQLiteOpenHelper helper = new MySQLiteOpenHelper(m_Activity);
            SQLiteDatabase db = helper.getReadableDatabase();
            ContentValues values = new ContentValues();

            values.put("sender_id", 0);
            m_text = m_text.replaceAll(CommonUtilities.LINE_BREAK_DODE, "\n").replaceAll(CommonUtilities.BLANK_CODE, " ");
            try {
                values.put("message", m_text.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            values.put("type", 0);
            values.put("group_id", m_group);
            db.insert("talk_table", null, values);

            SendMessage.setMessageYouTo(m_text);

            m_sl.scrollTo(0, m_sl.getBottom());

        } else if (my_user_data[0].equals("undefined_session_id")) {

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putBoolean("session_validness", false);
            editor.apply();

            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(m_Activity);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("friend_table", "_id like '%'", null);
            db.delete("talk_table", "_id like '%'", null);

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.Logout");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            m_Activity.startActivity(intent);
        } else if (ErrorMessages.ERROR_HASH_MAP.containsKey(my_user_data[0])) {
            Toast.makeText(m_Activity, ErrorMessages.ERROR_HASH_MAP.get(my_user_data[0]), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(m_Activity, my_user_data[0], Toast.LENGTH_LONG).show();
        }
    }

}