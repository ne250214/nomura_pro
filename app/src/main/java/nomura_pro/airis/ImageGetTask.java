package nomura_pro.airis;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ne250214 on 2016/01/05.
 */
class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
    private Context context;
    private int id;
    private int width;
    private int height;
    private LinearLayout ll;
    private ImageView imageView;

    public ImageGetTask(Context _context, int _id, int _width, int _height, LinearLayout _ll, ImageView _imageView) {
        context = _context;
        id = _id;
        width = _width;
        height = _height;
        ll = _ll;
        imageView = _imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap image;
        try {
            URL imageUrl = new URL(params[0]);
            InputStream imageIs;
            imageIs = imageUrl.openStream();
            image = BitmapFactory.decodeStream(imageIs);
            System.out.println("成功");
            return image;
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException");
            return null;
        } catch (IOException e) {
            System.out.println("IOException");
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        Bitmap resize_result;

        int[] resized = resize(width, height);

        imageView.setLayoutParams(new LinearLayout.LayoutParams(resized[0], resized[1]));
        try {

            resize_result = BitmapUtils.resize(result, resized[0], resized[1]);
            values.put("_id", id);
            values.put("image_byte", BitmapUtils.bmp2byteArray(resize_result, Bitmap.CompressFormat.JPEG));
            imageView.setImageBitmap(resize_result);
            db.update("news_table", values, "_id = ?", new String[]{String.valueOf(id)});

        } catch (NullPointerException e){
            System.out.println("NullPointerException");
            ll.removeAllViews();
        } catch (IllegalStateException e){
            System.out.println("IllegalStateException");
            ll.removeAllViews();
        }catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException");
            ll.removeAllViews();
        } finally {
            values = new ContentValues();

            values.put("width", resized[0]);
            values.put("height", resized[1]);
            db.update("news_table", values, "_id = ?", new String[]{String.valueOf(id)});
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
}