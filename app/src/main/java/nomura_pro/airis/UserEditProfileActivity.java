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
public class UserEditProfileActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        final SharedPreferences pref = getSharedPreferences("my_profile", MODE_PRIVATE);


        final EditText ET = (EditText) findViewById(R.id.editProfile);
        Button btn = (Button) findViewById(R.id.editProfileBtn);
        ET.setText(pref.getString("profile", ""));

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String profile = ET.getText().toString();

                profile = profile.replaceAll(" ",CommonUtilities.BLANK_CODE);

                UserEditProfileThread thread = new UserEditProfileThread(UserEditProfileActivity.this, pref);

                thread.execute(profile);
            }
        });
    }
}