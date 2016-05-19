package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by ne250214 on 15/09/17.
 */
public class LoginOrCreate extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_or_create);

        final SharedPreferences pref = getSharedPreferences("my_profile",MODE_PRIVATE);

        Button btnUserCreate = (Button)findViewById(R.id.user_create);
        Button btnLogin = (Button)findViewById(R.id.user_login);

        btnUserCreate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動
                Intent intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.UserCreateActivity");
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動

                final EditText addressET = (EditText) findViewById(R.id.Login_address);
                final EditText passET = (EditText) findViewById(R.id.Login_pass);

                UserLoginThread thread = new UserLoginThread(LoginOrCreate.this,pref);
                thread.execute(addressET.getText().toString(),passET.getText().toString());
            }
        });
    }
}
