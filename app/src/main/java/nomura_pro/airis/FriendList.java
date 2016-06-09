package nomura_pro.airis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ne250214 on 15/09/27.
 */
public class FriendList extends Fragment {

    Context context;
    SharedPreferences pref;
    LinearLayout layout;
    View v;

    SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.friend_list, container, false);
        layout = (LinearLayout) v.findViewById(R.id.friend_list_linerlayout);

        return v;
    }

    public void setDB() {

        Cursor c = db.query("friend_table", new String[]{"name", "profile", "screen_name", "type", "group_id"},
                "type=? OR type=? OR type=?", new String[]{"1", "2", "3"}, null, null, "name DESC");

        boolean isEof = c.moveToFirst();
        while (isEof) {
            final String name = c.getString(0).replaceAll(CommonUtilities.BLANK_CODE, " ");
            final String profile = c.getString(1).replaceAll(CommonUtilities.BLANK_CODE, " ");
            final String screen = c.getString(2);
            final int type = c.getInt(3);
            final int group_id = c.getInt(4);

            System.out.println(name);
            System.out.println(profile);
            System.out.println(screen);
            System.out.println(type);
            System.out.println(group_id);

            TextView tv = new TextView(context);

            final TextView nameTV = new TextView(context);
            nameTV.setText(name);
            nameTV.setTextSize(20);
            nameTV.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            nameTV.setTextColor(Color.BLACK);

            if (type == 1) {
                tv.setText(String.format("%s\n申請中", profile));

                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        type1onClick(name, screen);
                    }
                });
                nameTV.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        type1onClick(name, screen);
                    }
                });
            } else if (type == 2) {
                tv.setText(String.format("%s\n申請されています", profile));

                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        type2onClick(name, screen);
                    }
                });
                nameTV.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        type2onClick(name, screen);
                    }
                });
            } else if (type == 3) {
                tv.setText(String.format("%s\n友達", profile));


                tv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        type3onClick(name, screen, group_id);
                    }
                });
                nameTV.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        type3onClick(name, screen, group_id);
                    }
                });
            }
            isEof = c.moveToNext();

            layout.addView(nameTV);
            layout.addView(tv);

            TextView line = new TextView(context);
            line.setBackgroundResource(R.drawable.line);

            layout.addView(line);
        }

        c.close();

    }

    void type1onClick(String name, final String screen) {
        //選択項目を準備する。
        String[] str_items = {
                "申請を取り消す",
                "ブロックする",
                "キャンセル"};

        new AlertDialog.Builder(context)
                .setTitle(name + "さんに申請中です")
                .setItems(str_items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //選択したアイテムの番号(0～)がwhichに格納される
                                switch (which) {
                                    case 0:
                                        // 選択１
                                        FriendAddCancelThread fact = new FriendAddCancelThread((Activity) context, pref);
                                        fact.execute(screen);
                                        break;
                                    case 1:
                                        // 選択２
                                        FriendBlockThread fbt = new FriendBlockThread(context, pref);
                                        fbt.execute(screen);
                                        break;
                                    default:
                                        // キャンセル
                                        break;
                                }
                            }
                        }
                ).show();
    }

    void type2onClick(String name, final String screen) {
        //選択項目を準備する。
        String[] str_items = {
                "承認する",
                "ブロックする",
                "キャンセル"};

        new AlertDialog.Builder(context)
                .setTitle(name + "さんからの申請です")
                .setItems(str_items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                //選択したアイテムの番号(0～)がwhichに格納される
                                switch (which) {
                                    case 0:
                                        // 選択１
                                        FriendApproveThread fat = new FriendApproveThread((Activity) context, pref);
                                        fat.execute(screen);
                                        break;
                                    case 1:
                                        // 選択２
                                        FriendBlockThread fbt = new FriendBlockThread(context, pref);
                                        fbt.execute(screen);
                                        break;
                                    default:
                                        // キャンセル
                                        break;
                                }
                            }
                        }
                ).show();
    }

    void type3onClick(String name, final String screen, int group_id) {
        if (group_id == 0) {
            //トークルームを作っていなかったら
            System.out.println("friend_list");
            GroupParsonalLoginAndTalkListUpdateAndNewsGetThread nplantluangt = new GroupParsonalLoginAndTalkListUpdateAndNewsGetThread((Activity)context,pref,screen,name);
            nplantluangt.execute();
        } else {
            //トークルームを作っていたら

            Cursor c = db.query("group_table", new String[]{"room_id","last_updated"},
                    "_id=?", new String[]{String.valueOf(group_id)}, null, null, null);

            c.moveToFirst();
            String room_id = c.getString(0);
            long last_updatad = c.getLong(1);

            c.close();

            //Activity activity, SharedPreferences pref, String room_id, long last_updata,int group_id,String name,String screen
            TalkListUpdateAndNewsGetThread tluangt = new TalkListUpdateAndNewsGetThread((Activity) context,pref,room_id,last_updatad,group_id,name,screen);
            tluangt.execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        context = getActivity();
        pref = context.getSharedPreferences("my_profile", MODE_PRIVATE);

        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context);
        db = helper.getReadableDatabase();
        layout.removeAllViews();
        setDB();
    }

    @Override
    public void onPause() {
        super.onPause();
        db.close();
    }
}