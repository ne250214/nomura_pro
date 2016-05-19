package nomura_pro.airis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by ne250214 on 15/11/25.
 */
public class MyProfileFragment extends Fragment {

    TextView nameET;
    TextView addressET;
    TextView textET;
    TextView screen_nameET;

    Button btnName;
    Button btnProfile;
    Button btnPass;

    RadioGroup RadioGroup;
    RadioButton browse_true;
    RadioButton browse_false;

    SharedPreferences data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_profile, container, false);
        nameET = (TextView) v.findViewById(R.id.profile_name);
        addressET = (TextView) v.findViewById(R.id.profile_address);
        textET = (TextView) v.findViewById(R.id.profile_text);
        screen_nameET = (TextView) v.findViewById(R.id.profile_screen_name);

        btnName = (Button)v.findViewById(R.id.name_form);
        btnProfile = (Button)v.findViewById(R.id.profile_form);
        btnPass = (Button)v.findViewById(R.id.pass_form);

        RadioGroup = (RadioGroup)v.findViewById(R.id.browse_group);

        browse_true = (RadioButton) v.findViewById(R.id.browse_true);
        browse_false = (RadioButton) v.findViewById(R.id.browse_false);

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        final Activity context = getActivity();

        data = context.getSharedPreferences("my_profile", Context.MODE_PRIVATE);

        btnName.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動
                Intent intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.UserEditNameActivity");
                startActivity(intent);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動
                Intent intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.UserEditProfileActivity");
                startActivity(intent);
            }
        });

        btnPass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Sub 画面を起動
                Intent intent = new Intent();
                intent.setClassName("nomura_pro.airis", "nomura_pro.airis.UserEditPassActivity");
                startActivity(intent);
            }
        });

        String screen_name = data.getString("screen_name", "");
        String name = data.getString("name", "");
        name = name.replaceAll(CommonUtilities.BLANK_CODE, " ");
        String text = data.getString("profile", "").replaceAll(CommonUtilities.BLANK_CODE, " ");
        String address = data.getString("address", "");
        Boolean browse = data.getBoolean("browse_setting", true);

        nameET.setText(name);
        addressET.setText(address);
        textET.setText(text);
        screen_nameET.setText("@" + screen_name);

        if(browse){
            browse_true.setChecked(true);
        }
        else {
            browse_false.setChecked(true);
        }

        RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            // ラジオグループのチェック状態が変更された時に呼び出されます
            // チェック状態が変更されたラジオボタンのIDが渡されます
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences.Editor editor = data.edit();
                if(checkedId==R.id.browse_true){
                    editor.putBoolean("browse_setting", true);
                    editor.apply();
                }else if(checkedId==R.id.browse_false){
                    editor.putBoolean("browse_setting", false);
                    editor.apply();
                }
            }
        });
    }
}
