package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by ne250214 on 15/09/22.
 */
public class UserForceLoginThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;

    public UserForceLoginThread(Activity activity,SharedPreferences pref) {
        this.m_Activity = activity;
        this.m_pref = pref;
    }

    public final static String SENDER_ID = CommonUtilities.SENDER_ID;

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String address = params[0];
        String pass = params[1];

        String message = null;

        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this.m_Activity);
            final String regId = gcm.register(SENDER_ID);

            message = SocketConnect.connect("user force_login " + address + " " + pass + " " + regId);

        }catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {
        //accept Hash化したuser_id 名前 メールアドレス プロフィール 検索用id セッションid

        String[] my_user_data = param.split(" ", 0);

        if (my_user_data[0].equals("accept")){

            my_user_data[2] = my_user_data[2].replaceAll(CommonUtilities.BLANK_CODE, " ");
            my_user_data[4] = my_user_data[4].replaceAll(CommonUtilities.BLANK_CODE, " ");

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("screen_name", my_user_data[5]);
            editor.putString("id", my_user_data[1]);
            editor.putString("name", my_user_data[2]);
            editor.putString("address", my_user_data[3]);
            editor.putString("profile", my_user_data[4]);
            editor.putString("session", my_user_data[6]);
            editor.putBoolean("session_validness", true);
            editor.apply();

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.LoginCheck");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            m_Activity.startActivity(intent);
            m_Activity.finish();
        }
        else if(ErrorMessages.ERROR_HASH_MAP.containsKey(my_user_data[0])){
            Toast.makeText(m_Activity, ErrorMessages.ERROR_HASH_MAP.get(my_user_data[0]), Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(m_Activity, my_user_data[0], Toast.LENGTH_LONG).show();
        }
    }

}