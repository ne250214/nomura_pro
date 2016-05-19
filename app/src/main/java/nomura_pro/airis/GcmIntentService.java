package nomura_pro.airis;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Created by ne250214 on 15/08/26.
 */
public class GcmIntentService extends IntentService {
    private static final String TAG = "GcmIntentService";
    public GcmIntentService() {
        super("GcmIntentService");
    }
    private NotificationManager mManager;

    String name;
    String profile;
    String screen;
    String message;
    String type;

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.d(TAG, "messageType: " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d(TAG,"messageType: " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d(TAG,"messageType: " + messageType + ",body:" + extras.toString());
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);

        type = extras.getString("type");

        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (type != null) {
            switch (type) {
                case "no_contact":

                    name = extras.getString("name");
                    assert name != null;
                    name = name.replaceAll(CommonUtilities.BLANK_CODE, " ");
                    profile = extras.getString("profile");
                    assert profile != null;
                    profile = profile.replaceAll(CommonUtilities.BLANK_CODE, " ").replaceAll(CommonUtilities.LINE_BREAK_DODE, " ");
                    screen = extras.getString("search_id");

                    generateNotification(this);

                    values.put("name", name);
                    values.put("profile", profile);
                    values.put("screen_name",screen);
                    values.put("type", 2);
                    db.insert("friend_table", null, values);
                    break;

                case "reapplied":
                    name = extras.getString("name");
                    assert name != null;
                    name = name.replaceAll(CommonUtilities.BLANK_CODE, " ");
                    profile = extras.getString("profile");
                    assert profile != null;
                    profile = profile.replaceAll(CommonUtilities.BLANK_CODE, " ").replaceAll(CommonUtilities.LINE_BREAK_DODE, " ");
                    screen = extras.getString("search_id");

                    generateNotification(this);

                    values.put("name",name);
                    values.put("profile", profile);
                    values.put("screen_name",screen);
                    values.put("type", 3);
                    db.update("friend_table", values, "screen_name=?", new String[]{extras.getString("search_id")});
                    break;

                case "parsonal":
                    name = extras.getString("name");
                    assert name != null;
                    name = name.replaceAll(CommonUtilities.BLANK_CODE, " ");
                    screen = extras.getString("search_id");
                    message = extras.getString("message");
                    assert message != null;
                    message = message.replaceAll(CommonUtilities.LINE_BREAK_DODE, "\n").replaceAll(CommonUtilities.BLANK_CODE," ");

                    try {
                        Cursor c = db.query("friend_table", new String[]{"_id", "name", "profile", "screen_name", "type", "group_id"},
                                "screen_name=?", new String[]{screen}, null, null, "name DESC");
                        c.moveToFirst();

                        int id = c.getInt(0);

                        values.put("sender_id", id);
                        values.put("receiver_id", 0);
                        values.put("text", message);
                        db.insert("talk_table", null, values);
                    }catch ( android.database.CursorIndexOutOfBoundsException ignored){

                    }
                    generateNotification(this);

                    break;
                case "group":
                    break;
                default:
                    System.out.println("type ERROR");
                    break;
            }
        }

    }

    @SuppressLint("NewApi")
    private void generateNotification(Context context) {
        Intent notificationIntent = new Intent(context, Lunch.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        // LargeIcon の Bitmap を生成
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        Notification.Builder builder = new Notification.Builder(context);

        switch (type) {
            case "no_contact":
                builder.setTicker(name + "さんから申請されました");
                builder.setContentTitle(name + "さんからの申請です");
                //builder.setContentInfo("//名前。プロフィール");
                builder.setContentText(profile);
                break;
            case "reapplied":
                builder.setTicker(name + "さんに承認されました");
                builder.setContentTitle(name + "さんに承認されました");
                //builder.setContentInfo("//名前。プロフィール");
                builder.setContentText(profile);
                break;
            case "parsonal":
                builder.setTicker(name + "：" + message);
                builder.setContentTitle(name);
                builder.setContentText(message);

                try {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            SendMessage.catchMessage(name,message);
                        }
                    });
                }catch (java.lang.NullPointerException ignored){
                }
                break;
        }

        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pendingIntent);

        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setLargeIcon(largeIcon);

        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE
                | Notification.DEFAULT_LIGHTS);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, notification);
    }

}