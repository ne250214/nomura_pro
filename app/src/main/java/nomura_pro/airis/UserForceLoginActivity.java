package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by ne250214 on 15/09/22.
 */
public class UserForceLoginActivity extends Activity {

    //・login(メールアドレス、パスワード)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_force_login);

        final SharedPreferences pref = getSharedPreferences("my_profile",MODE_PRIVATE);

        Button btn = (Button) findViewById(R.id.force_login_btn);
        Button btn2 = (Button) findViewById(R.id.force_login_cancel);

        Intent i = getIntent();
        final String address = i.getStringExtra("address");
        final String pass = i.getStringExtra("pass");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserForceLoginThread thread = new UserForceLoginThread(UserForceLoginActivity.this,pref);
                thread.execute(address,pass);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.LoginCheck");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }


}