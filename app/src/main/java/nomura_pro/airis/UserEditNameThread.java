package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.widget.Toast;

/**
 * Created by ne250214 on 15/09/15.
 */
public class UserEditNameThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;

    public UserEditNameThread(Activity activity,SharedPreferences pref) {
        this.m_Activity = activity;
        this.m_pref = pref;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String name = params[0];

        String message;

        message = SocketConnect.connect("user edit_name " + m_pref.getString("id", "null") +" "+ m_pref.getString("session", "null") +" "+name);

        return message;
    }



    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        //accept 名前 セッションid

        String[] my_user_data = param.split(" ", 0);

        if (my_user_data[0].equals("accept")){

            my_user_data[1] = my_user_data[1].replaceAll(CommonUtilities.BLANK_CODE," ");

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("name", my_user_data[1]);
            editor.putString("session", my_user_data[2]);
            editor.apply();

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
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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