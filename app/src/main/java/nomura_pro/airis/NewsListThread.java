package nomura_pro.airis;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.HandlerThread;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Created by ne250214 on 16/05/21.
 */
public class NewsListThread  extends AsyncTask<String, Void, String> {

    private Activity m_Activity;
    SharedPreferences m_pref;
    int m_group_id;
    String m_room_id;

    int news_id[];

    public NewsListThread(Activity activity,SharedPreferences pref,int group_id,String room_id) {
        this.m_Activity = activity;
        this.m_pref = pref;
        this.m_group_id = group_id;
        this.m_room_id = room_id;
    }

    @Override
    protected String doInBackground(String... params) {

        HandlerThread SoketThread = new HandlerThread("SoketThread");
        SoketThread.start();

        String message;

        System.out.println("news list " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id);
        message = SocketConnect.connect("news list " + m_pref.getString("id", "null") + " " + m_pref.getString("session", "null") + " " + m_room_id);

        String[] my_user_data = message.split(" ", 3);

        MySQLiteOpenHelper dbHelper;
        SQLiteDatabase db;

        if (my_user_data[0].equals("accept")){

            SharedPreferences.Editor editor = m_pref.edit();
            editor.putString("session", my_user_data[1]);
            editor.apply();

            ContentValues values;

            news_id = new int[0];

            if(!(my_user_data[2].equals(""))) {

                try {
                    JSONArray json_arr = new JSONArray(my_user_data[2]);

                    news_id = new int[json_arr.length()];

                    for (int i = 0; i < json_arr.length(); i++) {
                        JSONObject json = (JSONObject) json_arr.get(i);

                        dbHelper = new MySQLiteOpenHelper(m_Activity);
                        db = dbHelper.getWritableDatabase();
                        long recodeCount = DatabaseUtils.queryNumEntries(db, "news_table", "_id = ?",
                                new String[]{json.getString("news_id")});

                        values = new ContentValues();

                        news_id[i] = json.getInt("news_id");

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


                                Bitmap resize_result;

                                int[] resized = resize(json.getInt("width"), json.getInt("height"));

                                try {
                                    System.out.println("url:" + json.getString("image_url"));
                                    Bitmap bitmap = downloadImage(json.getString("image_url"));
                                    resize_result = BitmapUtils.resize(bitmap.copy(Bitmap.Config.ARGB_8888, true), resized[0], resized[1]);
                                    values.put("image_byte", BitmapUtils.bmp2byteArray(resize_result, Bitmap.CompressFormat.JPEG));
                                }catch(java.lang.NullPointerException e){
                                    System.out.println("ぬるぽ");
                                } finally {
                                    System.out.println(json.getInt("width"));
                                    System.out.println(json.getInt("height"));
                                    System.out.println(resized[0]);
                                    System.out.println(resized[1]);
                                    values.put("width", resized[0]);
                                    values.put("height", resized[1]);
                                }


                            }

                            values.put("favorite", 0);

                            values.put("acquisition_date",d.getTime());

                            db.insert("news_table", null, values);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return message;
    }

    //タスクの終了後にUIスレッドとして起動
    @Override
    protected void onPostExecute(String param) {

        //accept セッションid JSON
        //[{title:値,date:値,url:値},{title:値,date:値,url:値},......]

        String[] my_user_data = param.split(" ", 3);

        MySQLiteOpenHelper dbHelper;
        SQLiteDatabase db;

        if (my_user_data[0].equals("accept")){

            Intent intent = new Intent();
            intent.setClassName("nomura_pro.airis", "nomura_pro.airis.NewsList");
            intent.putExtra("group_id", m_group_id);
            intent.putExtra("room_id", m_room_id);
            intent.putExtra("news_id", news_id);
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

    private int[] resize(int width, int height) {
        int[] resized = new int[2];
        //適当に縮小
        while (width > 350) {
            width = width * 9 / 10;
            height = height * 9 / 10;
        }
        resized[0] = width;
        resized[1] = height;

        return resized;
    }
    private Bitmap downloadImage(String address) {
        Bitmap bmp = null;

        try {
            URL url = new URL( address );

            // HttpURLConnection インスタンス生成
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // タイムアウト設定
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(20000);

            // リクエストメソッド
            urlConnection.setRequestMethod("GET");

            // リダイレクトを自動で許可しない設定
            urlConnection.setInstanceFollowRedirects(false);

            // ヘッダーの設定(複数設定可能)
            urlConnection.setRequestProperty("Accept-Language", "jp");

            // 接続
            urlConnection.connect();

            int resp = urlConnection.getResponseCode();

            switch (resp){
                case HttpURLConnection.HTTP_OK:
                    InputStream is = urlConnection.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                    is.close();
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            System.out.println("downloadImage error");
            e.printStackTrace();
        }

        return bmp;
    }

}