package nomura_pro.airis;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by ne250214 on 15/08/26.
 */
public class GcmActivity extends Activity {
    /** Google Cloud Messagingオブジェクト */
    private GoogleCloudMessaging gcm;
    /** コンテキスト */
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm_main);
        context = getApplicationContext();

        gcm = GoogleCloudMessaging.getInstance(this);
        registerInBackground();
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    String regid = gcm.register(CommonUtilities.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                TextView textView = (TextView)findViewById(R.id.textView);

                textView.setText(msg);

            }
        }.execute(null, null, null);
    }
}