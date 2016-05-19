package nomura_pro.airis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ne250214 on 15/12/09.
 */
public class Tweet {
    public static void tweeting(Context act, String Title, String Url) {
        String Template = "AIRISで共有！";
        String strTweet = "";
        String strHashTag = "#野村プロ2015";

        try {
            strTweet = "http://twitter.com/intent/tweet?text="
                    + URLEncoder.encode(Template, "UTF-8")
                    + "+"
                    + URLEncoder.encode("「"+Title+"」", "UTF-8")
                    + "+"
                    + URLEncoder.encode(strHashTag, "UTF-8")
                    + "&url="
                    + URLEncoder.encode(Url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(strTweet));
        act.startActivity(intent);
    }

}
