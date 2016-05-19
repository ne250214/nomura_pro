package nomura_pro.airis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ne250214 on 15/10/03.
 */
public class FriendBlockList extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("ブロックリスト");

        final SharedPreferences pref = getSharedPreferences("my_profile", MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        final Cursor c = db.query("friend_table", new String[]{"_id", "name", "profile", "screen_name", "type"},
                "type=?", new String[]{"0"}, null, null, "name DESC");


        boolean isEof = c.moveToFirst();
        while (isEof) {

            final int id = c.getInt(0);
            final String name = c.getString(1);
            final String profile = c.getString(2);
            final String screen = c.getString(3);
            final int type = c.getInt(4);

            TextView tv = new TextView(this);

            final TextView nameTV = new TextView(this);
            nameTV.setText(name);
            nameTV.setTextSize(20);
            nameTV.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            nameTV.setTextColor(Color.BLACK);


            tv.setText(String.format("%s\n%s\nブロック中", profile, "@"+screen));

            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //選択項目を準備する。
                    String[] str_items = {
                            "ブロックを解除する",
                            "キャンセル"};

                    new AlertDialog.Builder(FriendBlockList.this)
                            .setTitle(name + "さんをブロックしています")
                            .setItems(str_items, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            //選択したアイテムの番号(0～)がwhichに格納される
                                            switch (which) {
                                                case 0:
                                                    // 選択1
                                                    FriendBlockRemoveThread thread = new FriendBlockRemoveThread(FriendBlockList.this, pref);
                                                    thread.execute(screen);
                                                    break;
                                                default:
                                                    // キャンセル
                                                    break;
                                            }
                                        }
                                    }
                            ).show();
                }
            });

            isEof = c.moveToNext();

            layout.addView(nameTV);
            layout.addView(tv);
        }
        c.close();

        db.close();
    }
}