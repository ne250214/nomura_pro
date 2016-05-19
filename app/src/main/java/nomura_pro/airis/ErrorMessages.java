package nomura_pro.airis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ne250214 on 15/11/04.
 */
public class ErrorMessages {

    static final Map<String, String> ERROR_HASH_MAP = new HashMap<String, String>() {{
        put("no_type", "システムエラー");
        put("no_command", "システムエラー");
        put("contents_length", "システムエラー");

        put("name_length", "名前が長過ぎます");
        put("address_length", "メールアドレスが長過ぎます");
        put("password_length", "パスワードは8文字以上16文字以下でなければなりません");
        put("search_id_length", "検索idは8文字以上32文字以下で、英数字とアンダーバーのみ使用可能です");
        put("new_password_length", "パスワードは8文字以上16文字以下でなければなりません");

        put("address_format", "アドレスの形式が違います");
        put("password_format", "パスワードの形式が違います");
        put("search_id_format", "検索idの形式が違います");

        put("address_unique", "そのメールアドレスはすでに使われています");
        put("search_id_unique", "その検索idはすでに使われています");

        put("undefined_session_id", "セッションエラー");

        put("different_account", "パスワードが違います");
        put("different_password", "パスワードが違います");
        put("not_find", "ユーザーが見つかりません");

        put("different_computer", "");
        put("not_account", "システムエラー");
        put("undefined_user_id", "システムエラー");

        put("double_reapply", "すでに申請中です");
        put("double_friend", "すでに友達です");

        put("block", "ブロックしています");
        put("blocking", "すでにブロックしています");
        put("no_block", "ブロックしていません");
        put("no_friend", "友達ではありません");
        put("no_frined_type", "解除できる友達関係ではない");

        put("not_join_group", "このグループにはメッセージを送れません");


        put("Timeout", "サーバーとの接続に失敗しました");

        put("MessageNull", "サーバーとの接続に失敗しました");

    }};
}
