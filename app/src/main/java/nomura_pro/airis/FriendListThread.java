package nomura_pro.airis;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ne250214 on 15/09/29.
 */
public class FriendListThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;

    public FriendListThread(Activity activity,SharedPreferences pref) {
        this.m_Activity = activity;
        this.m_pref = pref;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        message = SocketConnect.connect("friend list " + m_pref.getString("id", "null") +" "+ m_pref.getString("session", "null"));

        return message;
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        //accept セッションid JSON
        //JSON {search_id: {name: 値, profile: 値}}
        //

        String[] my_user_data = param.split(" ", 0);

        MySQLiteOpenHelper dbHelper;
        SQLiteDatabase db;

        if (my_user_data[0].equals("accept")){

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            ContentValues values = new ContentValues();

            if(my_user_data.length==3) {

                try {
                    JSONArray json_arr = new JSONArray(my_user_data[2]);

                    for (int i = 0; i < json_arr.length(); i++) {
                        JSONObject json = (JSONObject) json_arr.get(i);

                        dbHelper = new MySQLiteOpenHelper(m_Activity);
                        db = dbHelper.getWritableDatabase();
                        long recodeCount = DatabaseUtils.queryNumEntries(db, "friend_table", "screen_name = ?", new String[]{json.getString("search_id")});

                        if(recodeCount==0) {
                            values.put("name", json.getString("name").replaceAll(CommonUtilities.BLANK_CODE, " "));
                            values.put("profile", json.getString("profile").replaceAll(CommonUtilities.BLANK_CODE, " "));
                            values.put("screen_name", json.getString("search_id"));
                            if (json.getString("type").equals("block")) {
                                values.put("type", 0);
                            } else if (json.getString("type").equals("reapplying")) {
                                values.put("type", 1);
                            } else if (json.getString("type").equals("reapplied")) {
                                values.put("type", 2);
                            } else if (json.getString("type").equals("friend")) {
                                values.put("type", 3);
                            }
                            db.insert("friend_table", null, values);
                        }if(recodeCount==1) {
                            values.put("name", json.getString("name").replaceAll(CommonUtilities.BLANK_CODE, " "));
                            values.put("profile", json.getString("profile").replaceAll(CommonUtilities.BLANK_CODE, " "));
                            if (json.getString("type").equals("block")) {
                                values.put("type", 0);
                            } else if (json.getString("type").equals("reapplying")) {
                                values.put("type", 1);
                            } else if (json.getString("type").equals("reapplied")) {
                                values.put("type", 2);
                            } else if (json.getString("type").equals("friend")) {
                                values.put("type", 3);
                            }
                            db.update("friend_table", values, "screen_name=?", new String[]{json.getString("search_id")});
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.MainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            m_Activity.startActivity(intent);
            m_Activity.finish();

        }
        else if (my_user_data[0].equals("undefined_session_id")){

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putBoolean("session_validness", false);
            editor.apply();

            dbHelper = new MySQLiteOpenHelper(m_Activity);
            db = dbHelper.getWritableDatabase();
            db.delete("friend_table", "_id like '%'", null);
            db.delete("talk_table", "_id like '%'", null);

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.Logout");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            m_Activity.startActivity(intent);
        }
        else if(ErrorMessages.ERROR_HASH_MAP.containsKey(my_user_data[0])){
            Toast.makeText(m_Activity, ErrorMessages.ERROR_HASH_MAP.get(my_user_data[0]), Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(m_Activity, my_user_data[0], Toast.LENGTH_LONG).show();
        }
    }

}