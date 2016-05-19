package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by ne250214 on 15/10/07.
 */
public class Logout extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logout);

        Button toLogin = (Button)findViewById(R.id.LogoutToLogin);

        toLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動
                Intent intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.UserLoginActivity");
                startActivity(intent);
            }
        });

    }
}