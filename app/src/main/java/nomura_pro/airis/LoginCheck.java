package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by ne250214 on 15/09/17.
 */
public class LoginCheck extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        final SharedPreferences pref = getSharedPreferences("my_profile",MODE_PRIVATE);

        String str = pref.getString("id", "");
        Boolean session = pref.getBoolean("session_validness", true);

        if (str.equals("")) {
            // ログイン画面を表示
            Intent intent = new Intent(getApplicationContext(),LoginOrCreate.class);
            startActivity(intent);
            this.finish();
        }
        else if(!session){
            Intent intent = new Intent(getApplicationContext(),Logout.class);
            startActivity(intent);
            this.finish();
        }
        else {
        //トップを表示
            FriendListThread thread = new FriendListThread(this,pref);
            thread.execute();
        }
    }
}