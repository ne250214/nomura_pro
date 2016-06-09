package nomura_pro.airis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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
 * Created by ne250214 on 15/10/13.
 */

public class SendMessageThread extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;
    LinearLayout m_layout;
    EditText m_textET;
    ScrollView m_sl;
    Button m_btn;

    int m_group;
    String m_room_id;
    String m_text;

    public SendMessageThread(Activity activity, SharedPreferences pref, LinearLayout layout, ScrollView sl, EditText textET, Button btn, int group_id, String room_id, String text) {
        this.m_Activity = activity;
        this.m_pref = pref;
        this.m_layout = layout;
        this.m_textET = textET;
        this.m_sl = sl;
        this.m_group = group_id;
        m_room_id = room_id;
        m_text = text;
        m_btn = btn;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        //message(ユーザ識別番号、セッションid、グループコード、メッセージ)
        message = SocketConnect.connect("send message " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id + " " + m_text);
        String[] my_user_data = message.split(" ", 0);
        if (my_user_data[0].equals("accept")) {

            System.out.println("send message");

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            MySQLiteOpenHelper helper = new MySQLiteOpenHelper(m_Activity);
            final SQLiteDatabase db = helper.getReadableDatabase();
            ContentValues values;

            Cursor c = db.query("group_table", new String[]{"last_updated"},
                    "_id=?", new String[]{String.valueOf(m_group)}, null, null, null);
            c.moveToFirst();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

            message = SocketConnect.connect("group talk " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id + " "+sdf.format(c.getLong(0)));

            my_user_data = message.split(" ", 3);

            if (my_user_data[0].equals("accept")) {

                System.out.println("group talk");

                //accept セッションid JSON(talk) JSON(news)(必要に応じて数が変化の可能性あり)
                //        JSON
                //                [talk:{user_id:値,(message:値/news_id:値),create_at:値},{user_id:値,(message:値/news_id:値),create_at:値}.....]
                //[news:{title:値,type_id:値,body:値,link_url:値,create_at:値,image_url:値,width:値,height:値}]

                editor = m_pref.edit();
                editor.putString("session", my_user_data[1]);
                editor.apply();

                String my_screen = m_pref.getString("screen_name", "");

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
                                values.put("group_id", m_group);
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
                                values.put("group_id", m_group);
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
                    System.out.println("News get");

                    String news_id_text = "";
                    for (int i = 0; i < news_id_list.size(); i++) {
                        if (i > 0) {
                            System.out.println(",を追加するよ");
                            news_id_text = news_id_text + ",";
                        }
                        news_id_text = news_id_text + news_id_list.get(i).toString();
                    }
                    message = SocketConnect.connect("news get " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + news_id_text);

                    my_user_data = message.split(" ", 3);

                    if (my_user_data[0].equals("accept")) {

                        System.out.println("news get");

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

        } else {
            return message;
        }
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        m_btn.setEnabled(true);

        String[] my_user_data = param.split(" ", 0);

        if (my_user_data[0].equals("accept")) {

            m_textET.setText("");

            SendMessage.setMessageYouTo(m_text);

            m_sl.scrollTo(0, m_sl.getBottom());

        } else if (my_user_data[0].equals("undefined_session_id")) {

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putBoolean("session_validness", false);
            editor.apply();

            MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(m_Activity);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
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