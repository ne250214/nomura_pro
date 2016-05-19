package nomura_pro.airis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by ne250214 on 15/09/17.
 */
public class Lunch extends Activity {

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // スプラッシュ用のビューを取得する
        setContentView(R.layout.lunch);

        final SharedPreferences pref = getSharedPreferences("my_profile",MODE_PRIVATE);

        // 2秒したらMainActivityを呼び出してSplashActivityを終了する
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // ログイン画面を表示
                Intent intent = new Intent(getApplicationContext(), LoginCheck.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);


                // SplashActivityを終了する
                Lunch.this.finish();
            }
        }, 2 * 1000); // 2000ミリ秒後（2秒後）に実行
    }
}