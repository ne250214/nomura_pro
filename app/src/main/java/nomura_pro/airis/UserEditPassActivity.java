package nomura_pro.airis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ne250214 on 15/07/10.
 */
public class UserEditPassActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_pass);

        final SharedPreferences pref = getSharedPreferences("my_profile", MODE_PRIVATE);

        Button btn = (Button) findViewById(R.id.editPassChange);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText passNowET = (EditText) findViewById(R.id.editPassNow);
                final EditText passNew1ET = (EditText) findViewById(R.id.editPassNew1);
                final EditText passNew2ET = (EditText) findViewById(R.id.editPassNew2);

                String[] user_data = new String[3];

                user_data[0] = passNowET.getText().toString();
                user_data[1] = passNew1ET.getText().toString();
                user_data[2] = passNew2ET.getText().toString();

                for (int i = 0; i < 2; i++) {
                    user_data[i] = user_data[i].replaceAll(" ",CommonUtilities.BLANK_CODE);
                }

                if (user_data[2].equals(user_data[1])) {

                    UserEditPassThread thread = new UserEditPassThread(UserEditPassActivity.this, pref);

                    thread.execute(user_data[0], user_data[1]);
                } else
                    Toast.makeText(UserEditPassActivity.this, "２つの新しいパスワードが一致していません", Toast.LENGTH_LONG).show();
            }
        });
    }
}