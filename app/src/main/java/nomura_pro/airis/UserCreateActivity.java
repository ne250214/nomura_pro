package nomura_pro.airis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ne250214 on 15/09/15.
 */
public class UserCreateActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_create);

        final SharedPreferences pref = getSharedPreferences("my_profile",MODE_PRIVATE);

        Button btn = (Button) findViewById(R.id.user_create_button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText nameET = (EditText) findViewById(R.id.user_create_name);
                final EditText addressET = (EditText) findViewById(R.id.user_create_address);
                final EditText passET = (EditText) findViewById(R.id.user_create_password);
                final EditText profileET = (EditText) findViewById(R.id.user_create_profile);
                final EditText user_idET = (EditText) findViewById(R.id.user_create_user_id);

                String[] user_data = new String[5];

                user_data[0] = nameET.getText().toString();
                user_data[1] = addressET.getText().toString();
                user_data[2] = passET.getText().toString();
                user_data[3] = profileET.getText().toString();
                user_data[4] = user_idET.getText().toString();

                for(int i=0;i<5;i++){
                    user_data[i] = user_data[i].replaceAll(" ",CommonUtilities.BLANK_CODE);
                }

                UserCreateThread thread = new UserCreateThread(UserCreateActivity.this,pref);

                thread.execute(user_data[0],user_data[1],user_data[2],user_data[3],user_data[4]);
            }
        });
    }


}


