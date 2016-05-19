package nomura_pro.airis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by ne250214 on 15/11/25.
 */
public class SocketConnect {

    public static String connect(String send){
        Socket connection = new Socket();
        BufferedReader reader = null;
        PrintWriter pw = null;
        String message;

        try {
            // サーバーへ接続
            connection.connect(new InetSocketAddress(CommonUtilities.SERVER_URL, CommonUtilities.SERVER_PORT),CommonUtilities.Default_TimeOut);

            // メッセージ取得オブジェクトのインスタンス化
            reader = new BufferedReader(new InputStreamReader(connection
                    .getInputStream()));

            pw = new PrintWriter(connection.getOutputStream(), true);

            pw.println(send);

            // サーバーからのメッセージを受信
            Thread.sleep(1000);
            message = reader.readLine();

            System.out.println(message);

        }catch (SocketTimeoutException ex){
            message ="Timeout";

        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
            message = "エラー内容：" + e.toString();
        }
        catch (java.lang.NullPointerException e){
            message = "MessageNull";
        } finally {
                // 接続終了処理
            try {
                connection.close();
                if (reader != null) {
                    reader.close();
                }
                if (pw != null) {
                    pw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return message;
    }

}
