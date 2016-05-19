package nomura_pro.airis;

/**
 * Created by ne250214 on 15/09/24.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "FriendData.db";

    private static final int DB_VERSION = 1;


    public MySQLiteOpenHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //type 0=ブロック中,1=申請中,2=申請され中,3=友達
        db.execSQL("create table friend_table ("
                + "_id integer primary key autoincrement,"
                + "name char(32) not null,"
                + "profile char(64),"
                + "screen_name varchar(32) not null,"
                + "type bit(4) not null,"
                + "group_id integer);");

        //talk_table sender_idまたはreceiver_idの0番は自分
        //type0=メセージ 1=ニュース
        //receiver_idいらなくね

        //db.execSQL("create table talk_table ("
         //       + "_id integer primary key autoincrement,"
         //       + "sender_id int not null,"
           //     + "receiver_id int not null,"
             //   + "type bit(2) not null);");

        //案
        //type=0 message type=1 news
        db.execSQL("create table talk_table ("
                + "_id integer primary key autoincrement,"
                + "group_id integer not null,"
                + "sender_id int not null,"
                + "message blob not null,"
                + "type bit(2) not null);");

        //type 0=個チャ、1グルチャ
        db.execSQL("create table group_table ("
                + "_id integer primary key autoincrement,"
                + "room_id char(32) not null,"
                + "name char(32),"
                + "type bit(2) not null);");

        //newsテーブルの_idはサーバのidと同じにする
        db.execSQL("create table news_table ("
                + "_id integer primary key ,"
                + "title char(32) not null,"
                + "type_id integer not null,"
                + "body char(64) not null,"
                + "link_url char(32) not null,"
                + "create_at char(32) not null,"
                + "image_url char(64) not null,"
                + "width integer,"
                + "height integer,"
                + "image_byte blob,"
                + "acquisition_date integer not null,"
                + "favorite bit(2) not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}