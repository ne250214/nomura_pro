package nomura_pro.airis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ne250214 on 15/09/22.
 */
public class UserLoginActivity extends Activity {

    //・login(メールアドレス、パスワード)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_or_create);

        final SharedPreferences pref = getSharedPreferences("my_profile",MODE_PRIVATE);

        Button btn = (Button) findViewById(R.id.user_login);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText addressET = (EditText) findViewById(R.id.Login_address);
                final EditText passET = (EditText) findViewById(R.id.Login_pass);

                UserLoginThread thread = new UserLoginThread(UserLoginActivity.this,pref);
                thread.execute(addressET.getText().toString(),passET.getText().toString());
            }
        });
    }


}

