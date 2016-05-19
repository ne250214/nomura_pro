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
public class UserEditNameActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_name);

        final SharedPreferences pref = getSharedPreferences("my_profile", MODE_PRIVATE);


        final EditText nameET = (EditText) findViewById(R.id.editName);
        Button btn = (Button) findViewById(R.id.editNameBtn);

        nameET.setText(pref.getString("name", ""));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameET.getText().toString();

                name = name.replaceAll(" ",CommonUtilities.BLANK_CODE);

                UserEditNameThread thread = new UserEditNameThread(UserEditNameActivity.this, pref);
                thread.execute(name);
            }
        });
    }
}