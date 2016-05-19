package nomura_pro.airis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by ne250214 on 15/09/20.
 */
public class FriendSearchActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_search);

        final SharedPreferences pref = getSharedPreferences("my_profile",MODE_PRIVATE);

        final Button btn = (Button) findViewById(R.id.FriendSearchBtn);
        final Button btnpost = (Button) findViewById(R.id.FriendSearchPostBtn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText ET = (EditText) findViewById(R.id.friend_search);

                String user_name = ET.getText().toString();

                final TextView screenTV = (TextView) findViewById(R.id.friend_search_screen);
                final TextView nameTV = (TextView) findViewById(R.id.friend_search_name);
                final TextView profileTV = (TextView) findViewById(R.id.friend_search_profile);

                FriendSearchThread thread = new FriendSearchThread(FriendSearchActivity.this,pref,screenTV,nameTV,profileTV,btnpost);

                thread.execute(user_name);
            }
        });

        btnpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add(ユーザ識別番号、セッションid、検索id)
                final TextView screenTV = (TextView) findViewById(R.id.friend_search_screen);
                final TextView nameTV = (TextView) findViewById(R.id.friend_search_name);
                final TextView profileTV = (TextView) findViewById(R.id.friend_search_profile);


                String btnText = btnpost.getText().toString();
                int type = 0;

                if(btnText.equals("友だち申請")){
                    type = 1;
                }
                else if(btnText.equals("友だちになる")){
                    type = 3;
                }


                String screen = screenTV.getText().toString();
                screen = screen.replaceAll("@","");
                String name = nameTV.getText().toString();
                String profile = profileTV.getText().toString();

                FriendAddThread thread = new FriendAddThread(FriendSearchActivity.this,pref,type);

                thread.execute(screen,name,profile);
            }
        });
    }


}





