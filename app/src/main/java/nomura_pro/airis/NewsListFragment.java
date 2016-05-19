package nomura_pro.airis;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ne250214 on 15/11/19.
 */
public class NewsListFragment extends Fragment {

    FragmentActivity activity;
    SharedPreferences pref;
    LinearLayout layout;
    View v;

    private Boolean[] fav_boolean;
    private static Boolean brows;

    SQLiteDatabase db;
    MySQLiteOpenHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.news_list, container, false);
        layout = (LinearLayout)v.findViewById(R.id.news_list_linerlayout);

        return v;
    }

    public void setDB() {

        Calendar calendar = Calendar.getInstance();

        Date date = null;
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month;
        if(10>calendar.get(Calendar.MONTH) + 1){
            month = "0"+String.valueOf(calendar.get(Calendar.MONTH) + 1);
        }else {
            month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        }
        String DATE;
        if(10>calendar.get(Calendar.DATE)){
            DATE = "0"+String.valueOf(calendar.get(Calendar.DATE));
        }else {
            DATE = String.valueOf(calendar.get(Calendar.DATE));
        }

        try {
            date = DateFormat.getDateInstance().parse(year+"/"+month+"/"+DATE);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Cursor c = null;
        try {
            assert date != null;

            c = db.query(false,"news_table", new String[]{"_id", "title", "type_id", "body", "link_url", "create_at", "image_url", "width", "height", "acquisition_date", "favorite","image_byte"},
                    null, null, null, null, "acquisition_date DESC","15");

            String acquisition_date = "";

            brows = pref.getBoolean("browse_setting", true);

            fav_boolean = new Boolean[c.getCount()];
            int i = 0;


            //new String[]{"_id", "title", "type_id", "body", "link_url", "create_at","image_url", "width", "height","acquisition_date", "favorite"}
            int type_id;
            String body;
            String create_at;
            String image_url;
            int width = 0;
            int height = 0;
            int server_news_id;

            int group_id;

            String acquisition_dateTmp;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");

            boolean isEof = c.moveToFirst();
            while (isEof) {

                    //new String[]{"_id", "title", "type_id", "body", "link_url", "create_at","image_url", "width", "height","acquisition_date", "favorite"}
                    final int id = c.getInt(0);
                    final String title = c.getString(1);
                    type_id = c.getInt(2);
                    body = c.getString(3);
                    final String link_url = c.getString(4);
                    create_at = c.getString(5);
                    image_url = c.getString(6);
                    width = c.getInt(7);
                    height = c.getInt(8);

                    final long data_long = c.getLong(9);

                    fav_boolean[i] = c.getInt(10) != 0;
                    byte[] blob = c.getBlob(11);

                    acquisition_dateTmp = sdf.format(data_long);

                    if (!acquisition_date.equals(acquisition_dateTmp)) {
                        acquisition_date = acquisition_dateTmp;
                        setDate(layout, acquisition_date);
                    }

                    setTitle(layout, title, link_url);

                    setBody(layout, body, link_url);

                    setImage(layout, image_url, blob, id, width, height, link_url);


                    TextView line = new TextView(activity);
                    line.setBackgroundResource(R.drawable.line);


                    LinearLayout l2 = new LinearLayout(activity);
                    l2.setOrientation(LinearLayout.HORIZONTAL);
                    l2.setGravity(Gravity.RIGHT);

                    final TextView genreTV = new TextView(activity);
                    String create_at_spl[] = create_at.split(" ", 3);
                    genreTV.setText(create_at_spl[0] + " " + returnGenre(type_id));
                    genreTV.setTextSize(15);
                    l2.addView(genreTV);

                    Space space = new Space(activity);
                    l2.addView(space, new LinearLayout.LayoutParams(20, 0));

                    setShareButton(l2,id);

                    //お気に入りボタンを置く
                    setFavoriteButton(l2, id, i);

                    //ツイートボタンを置く
                    setTweetButton(l2,title,link_url);

                    Space space2 = new Space(activity);
                    l2.addView(space2, new LinearLayout.LayoutParams(20, 0));

                    layout.addView(l2);
                    layout.addView(line);

                    i++;
                    isEof = c.moveToNext();
                }

        }
        finally {
            assert c != null;
            c.close();
            db.close();
        }


    }

    private void setImage(LinearLayout layout, String image_url, byte[] blob, int id, int width, int height, final String link_url) {
        if(!image_url.equals("null")) {
            //imageの表示場所

            ImageView image = new ImageView(activity);

            LinearLayout imageLay = new LinearLayout(activity);
            imageLay.setOrientation(LinearLayout.HORIZONTAL);
            imageLay.setGravity(Gravity.CENTER);

            imageLay.addView(image, width, height);
            layout.addView(imageLay);

            if (brows) {
                image.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        openLink(link_url);
                    }
                });
            } else {
                image.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        openLink_brows(link_url);
                    }
                });
            }


            try {
                image.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                image.setImageBitmap(BitmapUtils.createBitmap(blob, width, height));

            }catch (java.lang.NullPointerException e){
                ImageGetTask task = new ImageGetTask(activity, id, width, height, imageLay,image);
                task.execute(image_url);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        activity = getActivity();
        pref = activity.getSharedPreferences("my_profile", MODE_PRIVATE);

        helper = new MySQLiteOpenHelper(activity);
        db = helper.getReadableDatabase();

        layout.removeAllViews();
        setDB();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.close();
    }

    private void setDate(LinearLayout l,String date){
        TextView dateTV = new TextView(activity);
        dateTV.setGravity(Gravity.CENTER);
        dateTV.setText(date);
        dateTV.setTextSize(30);
        dateTV.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        dateTV.setTextColor(Color.WHITE);
        dateTV.setBackgroundColor(Color.parseColor("#87CEFA"));
        l.addView(dateTV);
    }

    private void setTitle(LinearLayout l,String title, final String link_url){
        TextView titleTV = new TextView(activity);
        titleTV.setText(title);
        titleTV.setTextSize(15);
        titleTV.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        titleTV.setTextColor(Color.BLACK);

        if (brows) {
            titleTV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openLink(link_url);
                }
            });
        } else {
            titleTV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openLink_brows(link_url);
                }
            });
        }


        l.addView(titleTV);
    }

    private void setBody(LinearLayout l, String body, final String link_url){
        TextView bodyTV = new TextView(activity);
        bodyTV.setText(body);
        bodyTV.setTextSize(15);

        if (brows) {
            bodyTV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openLink(link_url);
                }
            });
        } else {
            bodyTV.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openLink_brows(link_url);
                }
            });
        }

        l.addView(bodyTV);
    }

    private void openLink(String link_url){
        Intent intent = new Intent();
        intent.setClassName("nomura_pro.airis", "nomura_pro.airis.MyWebView");
        intent.putExtra("url", link_url);

        activity.startActivity(intent);
    }
    private void openLink_brows(String link_url){
        Uri uri = Uri.parse(link_url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }

    private void setFavoriteButton(LinearLayout l, final int id, final int i){
        final Button fav = new Button(activity);
        if (!fav_boolean[i]) {
            fav.setBackgroundResource(R.drawable.fav_btn);
        } else {
            fav.setBackgroundResource(R.drawable.fav_on);
        }


        fav.setOnClickListener(
                new View.OnClickListener() {

                    public void onClick(View v) {

                        MySQLiteOpenHelper dbHelper;
                        SQLiteDatabase db = null;
                        ContentValues values;
                        try {
                            dbHelper = new MySQLiteOpenHelper(activity);
                            db = dbHelper.getWritableDatabase();
                            values = new ContentValues();
                            if (fav_boolean[i]) {
                                fav.setBackgroundResource(R.drawable.fav_btn);
                                fav_boolean[i] = false;

                                values.put("favorite", 0);
                                db.update("news_table", values, "_id = ?", new String[]{String.valueOf(id)});

                            } else {
                                fav.setBackgroundResource(R.drawable.fav_on);
                                fav_boolean[i] = true;

                                values.put("favorite", 1);
                                db.update("news_table", values, "_id = ?", new String[]{String.valueOf(id)});
                            }
                        } finally {
                            assert db != null;
                            db.close();
                        }
                    }
                }

        );


        l.addView(fav, new LinearLayout.LayoutParams(100, 100));
    }

    private void setTweetButton(LinearLayout l,final String title, final String link_url){
        //Tweetボタン
        Button tweet_btn = new Button(activity);
        tweet_btn.setTextColor(Color.WHITE);
        tweet_btn.setBackgroundColor(Color.parseColor("#55acee"));
        tweet_btn.setText("Tweet");
        tweet_btn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Tweet.tweeting( activity, title, link_url);
                    }
                }
        );

        l.addView(tweet_btn);
    }

    private void setShareButton(LinearLayout l, final int id){
        //共有ボタン
        Button share_btn = new Button(activity);
        share_btn.setText("共有");
        share_btn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClassName("nomura_pro.airis", "nomura_pro.airis.NewsSelectRoom");
                        intent.putExtra("server_news_id", id);
                        startActivity(intent);
                    }
                }
        );

        l.addView(share_btn);
    }

    static String returnGenre(int type){
        String result = null;
        if(type == 1){
            result = "ニュース";
        }
        else if(type == 2){
            result = "エンタメ";
        }
        else if(type == 3){
            result = "スポーツ";
        }
        else if(type == 4){
            result = "ライフスタイル";
        }
        else if(type == 5){
            result = "テクノロジー";
        }
        else if(type == 11){
            result = "グルメ";
        }
        else if(type == 12){
            result = "旅行";
        }
        else if(type == 13){
            result = "ゲーム";
        }
        else if(type == 14){
            result = "アニメ";
        }
        return result;
    }
}