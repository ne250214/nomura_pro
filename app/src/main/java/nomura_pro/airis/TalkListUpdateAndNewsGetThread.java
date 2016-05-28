package nomura_pro.airis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ne250214 on 16/05/28.
 */
public class TalkListUpdateAndNewsGetThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;

    String m_room_id;
    String m_last_updata;

    int m_group_id;
    String m_name;
    String m_screen;

    public TalkListUpdateAndNewsGetThread(Activity activity, SharedPreferences pref, String room_id, long last_updata, int group_id, String name, String screen) {
        this.m_Activity = activity;
        this.m_pref = pref;
        m_room_id = room_id;

        if (last_updata == 0) {
            m_last_updata = CommonUtilities.BLANK_CODE;
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            m_last_updata = sdf.format(last_updata);
        }
        m_group_id = group_id;
        m_name = name;
        m_screen = screen;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        //talk (ユーザ識別番号、セッションid、グループコード、最終更新日時(0000-00-00 00:00:00)空白文字も可)
        message = SocketConnect.connect("group talk " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id + " " + m_last_updata);


        String[] my_user_data = message.split(" ", 3);

        if (my_user_data[0].equals("accept")) {

            //accept セッションid JSON(talk) JSON(news)(必要に応じて数が変化の可能性あり)
            //        JSON
            //                [talk:{user_id:値,(message:値/news_id:値),create_at:値},{user_id:値,(message:値/news_id:値),create_at:値}.....]
            //[news:{title:値,type_id:値,body:値,link_url:値,create_at:値,image_url:値,width:値,height:値}]

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            String my_screen = m_pref.getString("screen_name", "");

            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(m_Activity);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            ContentValues values;

            Cursor c;
            int sender_id;
            long recodeCount;
            ArrayList<Integer> news_id_list = new ArrayList<>();

            //ニュースを取得する必要があったらtrueにする
            boolean boolean_ngt = false;

            if (!(my_user_data[2].equals(""))) {

                try {
                    JSONArray json_arr = new JSONArray(my_user_data[2]);

                    for (int i = 0; i < json_arr.length(); i++) {
                        JSONObject json = (JSONObject) json_arr.get(i);

                        values = new ContentValues();


                        //db.execSQL("create table talk_table ("
                        //        + "_id integer primary key autoincrement,"
                        //        + "group_id integer not null,"
                        //        + "sender_id int not null,"
                        //        + "message blob not null,"
                        //       + "type bit(2) not null);");

                        //talk_table

                        if (my_screen.equals(json.getString("search_id"))) {
                            //自分が発言
                            sender_id = 0;
                        } else {
                            //他人が発言
                            c = db.query("friend_table", new String[]{"_id"},
                                    "screen_name=?", new String[]{json.getString("search_id")}, null, null, null);
                            c.moveToFirst();
                            sender_id = c.getInt(0);
                        }


                        try {
                            values.put("group_id", m_group_id);
                            values.put("sender_id", sender_id);
                            try {
                                values.put("message", json.getString("message").getBytes("UTF-8"));
                            } catch (UnsupportedEncodingException e1) {
                                e1.printStackTrace();
                            }

                            values.put("type", 0);

                            db.insert("talk_table", null, values);
                        } catch (org.json.JSONException e) {
                            //messageがなかったらニュース
                            boolean_ngt = true;
                            values = new ContentValues();
                            values.put("group_id", m_group_id);
                            values.put("sender_id", sender_id);
                            values.put("message", ByteBuffer.allocate(4).putInt(json.getInt("news_id")).array());
                            values.put("type", 1);

                            db.insert("talk_table", null, values);


                            recodeCount = DatabaseUtils.queryNumEntries(db, "news_table", "_id = ?", new String[]{json.getString("news_id")});
                            if (recodeCount == 0) {
                                boolean_ngt = true;
                                news_id_list.add(json.getInt("news_id"));
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            values = new ContentValues();
            Date d = new Date();
            values.put("last_updated", d.getTime());
            db.update("group_table", values, "room_id=?", new String[]{m_room_id});

            if (boolean_ngt) {
                //ニュースを取得する必要がある
                NewsGetThread newsGetThread = new NewsGetThread(m_Activity, m_pref, news_id_list, m_room_id, m_group_id, m_name, my_screen);
                newsGetThread.execute();

                String news_id_text = "";
                for (int i = 0; i < news_id_list.size(); i++) {
                    if (i > 0) {
                        System.out.println(",を追加するよ");
                        news_id_text = news_id_text + ",";
                    }
                    news_id_text = news_id_text + news_id_list.get(i).toString();
                }
                System.out.println("news get " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + news_id_text);
                message = SocketConnect.connect("news get " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + news_id_text);

                System.out.println("message"+message);
                my_user_data = message.split(" ", 3);

                if (my_user_data[0].equals("accept")) {

                    editor = m_pref.edit();
                    editor.putString("session", my_user_data[1]);
                    editor.apply();

                    if (!(my_user_data[2].equals(""))) {

                        try {
                            JSONArray json_arr = new JSONArray(my_user_data[2]);
                            //{news_id:値,title:値,type_id:値,body:値,link_url:値,create_at:値,image_url:値,width:値,height:値}

                            for (int i = 0; i < json_arr.length(); i++) {
                                JSONObject json = (JSONObject) json_arr.get(i);

                                recodeCount = DatabaseUtils.queryNumEntries(db, "news_table", "_id = ?",
                                        new String[]{json.getString("news_id")});

                                values = new ContentValues();

                                if (recodeCount == 0) {

                                    values.put("title", json.getString("title").replaceAll(CommonUtilities.BLANK_CODE, " "));
                                    values.put("type_id", json.getInt("type_id"));
                                    values.put("body", json.getString("body"));
                                    values.put("link_url", json.getString("link_url"));
                                    values.put("create_at", json.getString("create_at"));
                                    values.put("image_url", json.getString("image_url"));
                                    values.put("_id", json.getString("news_id"));
                                    if (!json.getString("image_url").equals("null")) {
                                        values.put("width", json.getInt("width"));
                                        values.put("height", json.getInt("height"));
                                    }

                                    values.put("favorite", 0);

                                    values.put("acquisition_date", d.getTime());

                                    db.insert("news_table", null, values);
                                }
                                if (recodeCount == 1) {
                                    values.put("title", json.getString("title").replaceAll(CommonUtilities.BLANK_CODE, " "));
                                    values.put("type_id", json.getInt("type_id"));
                                    values.put("body", json.getString("body"));
                                    values.put("link_url", json.getString("link_url"));
                                    values.put("create_at", json.getString("create_at"));
                                    values.put("image_url", json.getString("image_url"));
                                    values.put("_id", json.getString("news_id"));

                                    if (!json.getString("image_url").equals("null")) {
                                        values.put("width", json.getInt("width"));
                                        values.put("height", json.getInt("height"));
                                    }

                                    values.put("acquisition_date", d.getTime());

                                    db.update("news_table", values, "_id=?", new String[]{json.getString("news_id")});
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return message;
                } else {
                    return message;
                }
            }
            return message;
        } else {
            return message;
        }
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        System.out.println(param);

        //accept セッションid JSON
        //[{title:値,date:値,url:値},{title:値,date:値,url:値},......]

        String[] my_user_data = param.split(" ", 3);

        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(m_Activity);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (my_user_data[0].equals("accept")) {
            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.SendMessage");
            intent.putExtra("room_id", m_room_id);
            intent.putExtra("group_id", m_group_id);
            intent.putExtra("name", m_name);
            intent.putExtra("screen", m_screen);
            m_Activity.startActivity(intent);
        } else if (my_user_data[0].equals("undefined_session_id")) {

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putBoolean("session_validness", false);
            editor.apply();

            db.delete("friend_table", "_id like '%'", null);
            db.delete("talk_table", "_id like '%'", null);

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.Logout");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            m_Activity.startActivity(intent);
        } else if (ErrorMessages.ERROR_HASH_MAP.containsKey(my_user_data[0])) {
            Toast.makeText(m_Activity, ErrorMessages.ERROR_HASH_MAP.get(my_user_data[0]), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(m_Activity, my_user_data[0], Toast.LENGTH_LONG).show();
        }
    }

}