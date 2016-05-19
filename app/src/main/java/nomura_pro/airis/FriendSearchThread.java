package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ne250214 on 15/09/20.
 */
public class FriendSearchThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    private TextView m_screenTV;
    private TextView m_nameTV;
    private TextView m_profileTV;

    private Button m_btn;

    SharedPreferences m_pref;

    public FriendSearchThread(Activity activity,SharedPreferences pref,TextView screenTV,TextView nameTV,TextView profileTV,Button btn) {
        this.m_Activity = activity;
        this.m_pref = pref;
        this.m_screenTV = screenTV;
        this.m_nameTV = nameTV;
        this.m_profileTV = profileTV;

        this.m_btn = btn;

    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String user_name = params[0];

        String message;

        //search(ユーザ識別番号、セッションid、検索id)

        message = SocketConnect.connect("friend search " + m_pref.getString("id", "null") +" "+ m_pref.getString("session", "null") +" "+user_name);

        return message;
    }



    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        //accept 名前 プロフィール 検索id セッションid no_contact・reapplied

        String[] my_user_data = param.split(" ", 0);

        if (my_user_data[0].equals("accept")){

            my_user_data[1] = my_user_data[1].replaceAll(CommonUtilities.BLANK_CODE, " ");
            my_user_data[2] = my_user_data[2].replaceAll(CommonUtilities.BLANK_CODE, " ");


            m_screenTV.setText("@"+my_user_data[3]);
            m_nameTV.setText(my_user_data[1]);
            m_profileTV.setText(my_user_data[2]);


            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[4]);
            editor.apply();


            if (m_btn.getVisibility() != View.VISIBLE) {
                m_btn.setVisibility(View.VISIBLE);
            }
            if(my_user_data[5].equals("no_contact")){
                m_btn.setText("友だち申請");
            }
            else if(my_user_data[5].equals("reapplied")){
                m_btn.setText("友だちになる");
            }

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