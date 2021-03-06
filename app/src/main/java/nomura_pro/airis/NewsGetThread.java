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

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ne250214 on 16/05/25.
 */
public class NewsGetThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;
    String m_news_id_text;

    String m_room_id;
    int m_group_id;
    String m_name;
    String m_screen;

    public NewsGetThread(Activity activity,SharedPreferences pref, ArrayList<Integer> news_id_list,String room_id,int group_id,String name,String screen){
        m_Activity = activity;
        m_pref = pref;

        m_room_id = room_id;
        m_group_id = group_id;
        m_name = name;
        m_screen = screen;

        m_news_id_text = "";
        for(int i=0;i<news_id_list.size();i++){
            if(i>1) m_news_id_text = m_news_id_text + ",";
            m_news_id_text = m_news_id_text + news_id_list.get(i).toString();
        }
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        //get(ユーザ識別番号、セッションid、ニュースid(カンマ区切り)例:1,2,3,4,5)
        message = SocketConnect.connect("news get " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_news_id_text);
        System.out.println(SocketConnect.connect("news get " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_news_id_text));

        return message;
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        System.out.println(param);

        //accept セッションid JSON
        //[{title:値,date:値,url:値},{title:値,date:値,url:値},......]

        String[] my_user_data = param.split(" ", 3);

        MySQLiteOpenHelper dbHelper= new MySQLiteOpenHelper(m_Activity);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (my_user_data[0].equals("accept")){

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            ContentValues values;

            if(!(my_user_data[2].equals(""))) {

                try {
                    JSONArray json_arr = new JSONArray(my_user_data[2]);
                    //{news_id:値,title:値,type_id:値,body:値,link_url:値,create_at:値,image_url:値,width:値,height:値}

                    for (int i = 0; i < json_arr.length(); i++) {
                        JSONObject json = (JSONObject) json_arr.get(i);

                        long recodeCount = DatabaseUtils.queryNumEntries(db, "news_table", "_id = ?",
                                new String[]{json.getString("news_id")});

                        values = new ContentValues();

                        Date d = new Date();

                        if(recodeCount==0) {

                            values.put("title", json.getString("title").replaceAll(CommonUtilities.BLANK_CODE, " "));
                            values.put("type_id", json.getInt("type_id"));
                            values.put("body", json.getString("body"));
                            values.put("link_url", json.getString("link_url"));
                            values.put("create_at", json.getString("create_at"));
                            values.put("image_url", json.getString("image_url"));
                            values.put("_id", json.getString("news_id"));
                            if(!json.getString("image_url").equals("null")) {
                                values.put("width", json.getInt("width"));
                                values.put("height", json.getInt("height"));
                            }

                            values.put("favorite", 0);

                            values.put("acquisition_date",d.getTime());

                            db.insert("news_table", null, values);
                        }if(recodeCount==1) {
                            values.put("title", json.getString("title").replaceAll(CommonUtilities.BLANK_CODE, " "));
                            values.put("type_id", json.getInt("type_id"));
                            values.put("body", json.getString("body"));
                            values.put("link_url", json.getString("link_url"));
                            values.put("create_at", json.getString("create_at"));
                            values.put("image_url", json.getString("image_url"));
                            values.put("_id", json.getString("news_id"));

                            if(!json.getString("image_url").equals("null")) {
                                values.put("width", json.getInt("width"));
                                values.put("height", json.getInt("height"));
                            }

                            values.put("acquisition_date",d.getTime());

                            db.update("news_table", values, "_id=?", new String[]{json.getString("news_id")});
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.SendMessage");
            intent.putExtra("room_id", m_room_id);
            intent.putExtra("group_id", m_group_id);
            intent.putExtra("name", m_name);
            intent.putExtra("screen", m_screen);
            m_Activity.startActivity(intent);
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