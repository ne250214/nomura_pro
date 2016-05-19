package nomura_pro.airis;

/**
 * Created by ne250214 on 2016/01/29.
 */

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;

public class BitmapUtils {

    /**
     * ファイルからBitpmap生成 表示サイズ合わせて画像生成時に可能なかぎり縮小して生成します。
     *
     * @param path
     *            パス
     * @param width
     *            表示幅
     * @param height
     *            表示高さ
     * @return 生成Bitmap
     */
    public static Bitmap createBitmap(String path, int width, int height) {

        BitmapFactory.Options option = new BitmapFactory.Options();

        // 情報のみ読み込む
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, option);

        if (option.outWidth < width || option.outHeight < height) {
            // 縦、横のどちらかが指定値より小さい場合は普通にBitmap生成
            return BitmapFactory.decodeFile(path);
        }

        float scaleWidth = ((float) width) / option.outWidth;
        float scaleHeight = ((float) height) / option.outHeight;

        int newSize = 0;
        int oldSize = 0;
        if (scaleWidth > scaleHeight) {
            newSize = width;
            oldSize = option.outWidth;
        } else {
            newSize = height;
            oldSize = option.outHeight;
        }

        // option.inSampleSizeに設定する値を求める
        // option.inSampleSizeは2の乗数のみ設定可能
        int sampleSize = 1;
        int tmpSize = oldSize;
        while (tmpSize > newSize) {
            sampleSize = sampleSize * 2;
            tmpSize = oldSize / sampleSize;
        }
        if (sampleSize != 1) {
            sampleSize = sampleSize / 2;
        }

        option.inJustDecodeBounds = false;
        option.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(path, option);
    }

    /**
     * バイト配列からBitmap生成 表示サイズ合わせて画像生成時に可能なかぎり縮小して生成します。
     *
     * @param bytes
     *            バイト配列の画像
     * @param width
     *            表示幅
     * @param height
     *            表示高さ
     * @return 生成Bitmap
     */
    public static Bitmap createBitmap(byte[] bytes, int width, int height) {

        BitmapFactory.Options option = new BitmapFactory.Options();

        // 情報のみ読み込む
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, option);

        if (option.outWidth < width || option.outHeight < height) {
            // 縦、横のどちらかが指定値より小さい場合は普通にBitmap生成
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        float scaleWidth = ((float) width) / option.outWidth;
        float scaleHeight = ((float) height) / option.outHeight;

        int newSize = 0;
        int oldSize = 0;
        if (scaleWidth > scaleHeight) {
            newSize = width;
            oldSize = option.outWidth;
        } else {
            newSize = height;
            oldSize = option.outHeight;
        }

        // option.inSampleSizeに設定する値を求める
        // option.inSampleSizeは2の乗数のみ設定可能
        int sampleSize = 1;
        int tmpSize = oldSize;
        while (tmpSize > newSize) {
            sampleSize = sampleSize * 2;
            tmpSize = oldSize / sampleSize;
        }
        if (sampleSize != 1) {
            sampleSize = sampleSize / 2;
        }

        option.inJustDecodeBounds = false;
        option.inSampleSize = sampleSize;

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, option);
    }

    /**
     * バイト配列からBitpmap生成 表示サイズ合わせて拡大縮小します。
     *
     * @param bytes
     *            バイト配列の画像
     * @param width
     *            表示幅
     * @param height
     *            表示高さ
     * @return 生成Bitmap
     */
    public static Bitmap createScaleBitmap(byte[] bytes, int width, int height) {

        Bitmap bm = byte2bmp(bytes);
        Matrix matrix = new Matrix();

        float xScale = (float) width / bm.getWidth() * 0.8f;
        float yScale = (float) height / bm.getHeight() * 0.8f;

        matrix.postScale(xScale, yScale);

        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

    }

    /**
     * ファイルからBitpmap生成 表示サイズ合わせて拡大縮小します。
     *
     * @param path
     *            パス
     * @param width
     *            表示幅
     * @param height
     *            表示高さ
     * @return 生成Bitmap
     */
    public static Bitmap createScaleBitmap(String path, int width, int height) {

        InputStream is;
        Bitmap bm = null;
        Matrix matrix = new Matrix();
        try {
            is = new FileInputStream(path);
            bm = BitmapFactory.decodeStream(is);
            float xScale = (float) width / bm.getWidth();
            float yScale = (float) height / bm.getHeight();

            matrix.postScale(xScale, yScale);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

    }

    /**
     * 画像リサイズ
     *
     * @param bitmap
     *            変換対象ビットマップ
     * @param newWidth
     *            変換サイズ横
     * @param newHeight
     *            変換サイズ縦
     * @return 変換後Bitmap
     */
    public static Bitmap resize(Bitmap bitmap, int newWidth, int newHeight) {

        if (bitmap == null) {
            return null;
        }

        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();

        if (oldWidth < newWidth && oldHeight < newHeight) {
            // 縦も横も指定サイズより小さい場合は何もしない
            return bitmap;
        }

        float scaleWidth = ((float) newWidth) / oldWidth;
        float scaleHeight = ((float) newHeight) / oldHeight;
        float scaleFactor = Math.min(scaleWidth, scaleHeight);

        Matrix scale = new Matrix();
        scale.postScale(scaleFactor, scaleFactor);

        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, scale, false);
        bitmap.recycle();

        return resizeBitmap;

    }

    /**
     *
     * bitmapをバイト配列に変換します。
     *
     * @param bitmap
     *            ビットマップ
     * @param format
     *            圧縮フォーマット
     * @return
     */
    public static byte[] bmp2byteArray(Bitmap bitmap, CompressFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();

    }

    /**
     * bitmapをバイト配列に変換します。
     *
     * @param bitmap
     *            ビットマップ
     * @param format
     *            圧縮フォーマット
     * @param compressVal
     *            圧縮率
     * @return
     */
    public static byte[] bmp2byteArray(Bitmap bitmap, CompressFormat format, int compressVal) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, compressVal, baos);
        return baos.toByteArray();

    }

    /**
     * バイト配列をbitmapに変換します。
     *
     * @param bytes
     * @return
     */
    public static Bitmap byte2bmp(byte[] bytes) {
        Bitmap bmp = null;
        if (bytes != null) {
            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        return bmp;
    }

    /**
     * バイト配列を圧縮して、返却します。
     *
     * @param bytes
     *            バイト配列
     * @param format
     *            圧縮フォーマット
     * @param compressVal
     *            圧縮率
     * @return
     */
    public static byte[] compressBitmap(byte[] bytes, CompressFormat format, int compressVal) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapUtils.byte2bmp(bytes).compress(format, compressVal, baos);
        return baos.toByteArray();

    }

    /**
     * 画像をモノクロに変換します。
     *
     * @param bmp
     * @return
     */
    public static Bitmap monocrome(Bitmap bmp) {
        // モノクロにする処理
        Bitmap outBitMap = bmp.copy(Bitmap.Config.ARGB_8888, true);

        int width = outBitMap.getWidth();
        int height = outBitMap.getHeight();
        int totalPixcel = width * height;

        int i, j;
        for (j = 0; j < height; j++) {
            for (i = 0; i < width; i++) {
                int pixelColor = outBitMap.getPixel(i, j);
                int y = (int) (0.299 * Color.red(pixelColor) + 0.587 * Color.green(pixelColor) + 0.114 * Color
                        .blue(pixelColor));
                outBitMap.setPixel(i, j, Color.rgb(y, y, y));
            }
        }

        return outBitMap;
    }

}
