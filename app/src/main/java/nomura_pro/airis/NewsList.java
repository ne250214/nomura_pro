package nomura_pro.airis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

/**
 * Created by ne250214 on 15/11/20.
 */
public class NewsList extends Activity {

    int group_id;
    String room_id;
    int[] news_id;

    LinearLayout layout;
    SQLiteDatabase db;
    Boolean fav_boolean[];
    Boolean brows;
    MySQLiteOpenHelper helper;
    SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        group_id = i.getIntExtra("group_id", 0);
        room_id = i.getStringExtra("room_id");
        news_id = i.getIntArrayExtra("news_id");

        setContentView(R.layout.news_list);
        layout = (LinearLayout)findViewById(R.id.news_list_linerlayout);

    }
    @Override
    protected void onResume() {
        super.onResume();

        layout.removeAllViews();

        helper = new MySQLiteOpenHelper(getApplicationContext());
        db = helper.getReadableDatabase();

        pref = getSharedPreferences("my_profile", MODE_PRIVATE);
        Cursor c = null;
        try {

            String acquisition_date = "";

            brows = pref.getBoolean("browse_setting", true);

            int i;

            fav_boolean = new Boolean[news_id.length];

            int type_id;
            String body;
            String create_at;
            String image_url;
            int width;
            int height;

            String acquisition_dateTmp;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");

            if (news_id.length == 0) {
                Toast.makeText(this, "ニュースがありません", Toast.LENGTH_LONG).show();
            }else{
            for (i = 0; i < news_id.length; i++) {

                c = db.query(false, "news_table", new String[]{"_id", "title", "type_id", "body", "link_url", "create_at", "image_url", "width", "height", "acquisition_date", "favorite", "image_byte"},
                        "_id=?", new String[]{String.valueOf(news_id[i])}, null, null, "acquisition_date DESC", "20");
                c.moveToFirst();

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

                System.out.println("----------");
                System.out.println(title);
                System.out.println(body);
                System.out.println(link_url);
                System.out.println(create_at);
                System.out.println(width);
                System.out.println(height);
                System.out.println("----------");

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


                TextView line = new TextView(getApplicationContext());
                line.setBackgroundResource(R.drawable.line);


                LinearLayout l2 = new LinearLayout(getApplicationContext());
                l2.setOrientation(LinearLayout.HORIZONTAL);
                l2.setGravity(Gravity.RIGHT);

                final TextView genreTV = new TextView(getApplicationContext());
                String create_at_spl[] = create_at.split(" ", 3);
                genreTV.setText(create_at_spl[0] + " " + returnGenre(type_id));
                genreTV.setTextSize(15);
                genreTV.setTextColor(Color.BLACK);
                l2.addView(genreTV);

                Space space = new Space(getApplicationContext());
                l2.addView(space, new LinearLayout.LayoutParams(20, 0));

                setShareButton(l2, id);

                //お気に入りボタンを置く
                setFavoriteButton(l2, id, i);

                //ツイートボタンを置く
                setTweetButton(l2, title, link_url);

                Space space2 = new Space(getApplicationContext());
                l2.addView(space2, new LinearLayout.LayoutParams(20, 0));

                layout.addView(l2);
                layout.addView(line);

            }
        }
        }finally {
            db.close();
            if (c != null) {
                c.close();
            }
        }


    }

    @Override
    public void onPause(){
        super.onPause();
        db.close();
    }
    private void setImage(LinearLayout l, String image_url, byte[] blob,int id, int width, int height, final String link_url){
        if(!image_url.equals("null")) {
            //imageの表示場所

                ImageView image = new ImageView(getApplicationContext());

                LinearLayout imageLay = new LinearLayout(getApplicationContext());
                imageLay.setOrientation(LinearLayout.HORIZONTAL);
                imageLay.setGravity(Gravity.CENTER);

                imageLay.addView(image, width, height);
                l.addView(imageLay);

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

                }catch (java.lang.NullPointerException ignored){
                }
        }
    }

    private void setDate(LinearLayout l,String date){
        TextView dateTV = new TextView(getApplicationContext());
        dateTV.setGravity(Gravity.CENTER);
        dateTV.setText(date);
        dateTV.setTextSize(30);
        dateTV.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        dateTV.setTextColor(Color.WHITE);
        dateTV.setBackgroundColor(Color.parseColor("#87CEFA"));
        l.addView(dateTV);
    }

    private void setTitle(LinearLayout l,String title, final String link_url){
        TextView titleTV = new TextView(getApplicationContext());
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
        TextView bodyTV = new TextView(getApplicationContext());
        bodyTV.setText(body);
        bodyTV.setTextSize(15);
        bodyTV.setTextColor(Color.BLACK);

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
        startActivity(intent);
    }
    private void openLink_brows(String link_url){
        Uri uri = Uri.parse(link_url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void setFavoriteButton(LinearLayout l, final int id, final int i){
        final Button fav = new Button(getApplication());
        if (!fav_boolean[i]) {
            fav.setBackgroundResource(R.drawable.fav_btn);
        } else {
            fav.setBackgroundResource(R.drawable.fav_on);
        }

        fav.setOnClickListener(
                new View.OnClickListener() {

                    public void onClick(View v) {
                        ContentValues values;
                        try {
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
        Button tweet_btn = new Button(getApplication());
        tweet_btn.setTextColor(Color.WHITE);
        tweet_btn.setBackgroundColor(Color.parseColor("#55acee"));
        tweet_btn.setText("Tweet");
        tweet_btn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Tweet.tweeting(getApplication(), title, link_url);
                    }
                }
        );

        l.addView(tweet_btn);
    }

    private void setShareButton(LinearLayout l, final int id){
        //共有ボタン
        Button share_btn = new Button(getApplication());
        share_btn.setText("共有");
        share_btn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        // 確認ダイアログの生成
                        AlertDialog.Builder alertDlg = new AlertDialog.Builder(NewsList.this);
                        alertDlg.setTitle("共有確認");
                        alertDlg.setPositiveButton(
                                "このトークルームに共有",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which){
                                        SendNewsThread thread = new SendNewsThread(NewsList.this, pref,room_id,group_id,id);
                                        thread.execute();
                                    }
                                });
                        alertDlg.setNegativeButton(
                                "他のトークルームに共有",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent();
                                        intent.setClassName("nomura_pro.airis", "nomura_pro.airis.NewsSelectRoom");
                                        intent.putExtra("server_news_id", id);
                                        startActivity(intent);
                                    }
                                });

                        // 表示
                        alertDlg.create().show();
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
