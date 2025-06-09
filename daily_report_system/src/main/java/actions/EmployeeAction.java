package actions;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;

import actions.views.EmployeeView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import constants.PropertyConst;
import services.EmployeeService;

/**
 * 従業員に関わる処理を行うActionクラス
 */
public class EmployeeAction extends ActionBase {

    private EmployeeService service;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        service = new EmployeeService();

        // メソッドを実行
        invoke();

        service.close();
    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        if (checkAdmin()) {
        // 指定されたページ数の一覧画面に表示するデータを取得
        int page = getPage();
        List<EmployeeView> employees = service.getPerPage(page);

        // 全ての従業員データの件数を取得
        long employeeCount = service.countAll();

        putRequestScope(AttributeConst.EMPLOYEES, employees); // 取得した従業員データ
        putRequestScope(AttributeConst.EMP_COUNT, employeeCount); // 全ての従業員データの件数
        putRequestScope(AttributeConst.PAGE, page); // ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); // 1ページに表示するレコードの数

        // セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        // 一覧画面を表示
        forward(ForwardConst.FW_EMP_INDEX);
    }
    }

    /**
     * 新規登録画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException, IOException {

        if (checkAdmin()) {
        putRequestScope(AttributeConst.TOKEN, getTokenId()); // CSRF対策用トークン
        putRequestScope(AttributeConst.EMPLOYEE, new EmployeeView()); // 空の従業員インスタンス

        // 新規登録画面を表示
        forward(ForwardConst.FW_EMP_NEW);
    }
    } 
    
    /**
     * 登録処理を行う
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException, IOException {

        if (checkAdmin()) {
        // CSRF対策 tokenのチェック
        if (checkToken()) {

            // パラメータの値を元に従業員情報のインスタンスを作成する
            EmployeeView ev = new EmployeeView(
                    null,
                    getRequestParam(AttributeConst.EMP_CODE),
                    getRequestParam(AttributeConst.EMP_NAME),
                    getRequestParam(AttributeConst.EMP_PASS),
                    Integer.parseInt(getRequestParam(AttributeConst.EMP_ADMIN_FLG)),
                    null,
                    null,
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue());

            // アプリケーションスコープからpepper文字列を取得
            String pepper = getContextScope(PropertyConst.PEPPER);

            // 従業員情報登録
            List<String> errors = service.create(ev, pepper);

            if (errors.size() > 0) {
                // 登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); // CSRF対策用トークン
                putRequestScope(AttributeConst.EMPLOYEE, ev); // 入力された従業員情報
                putRequestScope(AttributeConst.ERR, errors); // エラーのリスト

                // 新規登録画面を再表示
                forward(ForwardConst.FW_EMP_NEW);
            } else {
                // 登録中にエラーがなかった場合

                // セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                // 一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }
    }

    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void show() throws ServletException, IOException {

        if (checkAdmin()) {
        // idを条件に従業員データを取得する
        EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));

        if (ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {

            // データが取得できなかった、または論理削除されている場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return;
        }

        putRequestScope(AttributeConst.EMPLOYEE, ev); // 取得した従業員情報

        // 詳細画面を表示
        forward(ForwardConst.FW_EMP_SHOW);
    }
    }

    /**
     * 編集画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void edit() throws ServletException, IOException {

        if (checkAdmin()) {
        // idを条件に従業員データを取得する
        EmployeeView ev = service.findOne(toNumber(getRequestParam(AttributeConst.EMP_ID)));

        if (ev == null || ev.getDeleteFlag() == AttributeConst.DEL_FLAG_TRUE.getIntegerValue()) {

            // データが取得できなかった、または論理削除されている場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);
            return;
        }

        putRequestScope(AttributeConst.TOKEN, getTokenId()); // CSRF対策用トークン
        putRequestScope(AttributeConst.EMPLOYEE, ev); // 取得した従業員情報

        // 編集画面を表示する
        forward(ForwardConst.FW_EMP_EDIT);
    }
    }

    /**
     * 更新処理を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update() throws ServletException, IOException {

        if (checkAdmin()) {
        // CSRF対策 tokenのチェック
        if (checkToken()) {
            // パラメータの値を元に従業員情報のインスタンスを作成する
            EmployeeView ev = new EmployeeView(
                    toNumber(getRequestParam(AttributeConst.EMP_ID)), // id
                    getRequestParam(AttributeConst.EMP_CODE), // 社員番号
                    getRequestParam(AttributeConst.EMP_NAME), // 氏名
                    getRequestParam(AttributeConst.EMP_PASS), // パスワード
                    Integer.parseInt(getRequestParam(AttributeConst.EMP_ADMIN_FLG)), // 管理者権限
                    null, // 登録日時
                    null, // 更新日時
                    AttributeConst.DEL_FLAG_FALSE.getIntegerValue()); // 削除フラグ

            // アプリケーションスコープからpepper文字列を取得
            String pepper = getContextScope(PropertyConst.PEPPER);

            // 従業員情報更新
            List<String> errors = service.update(ev, pepper);

            if (errors.size() > 0) {
                // 更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); // CSRF対策用トークン
                putRequestScope(AttributeConst.EMPLOYEE, ev); // 入力された従業員情報
                putRequestScope(AttributeConst.ERR, errors); // エラーのリスト

                // 編集画面を再表示
                forward(ForwardConst.FW_EMP_EDIT);
            } else {
                // 更新中にエラーがなかった場合

                // セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                // 一覧画面にリダイレクト
                redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
            }
        }
    }
    }
    
    /**
     * 論理削除を行う
     * @throws ServletException
     * @throws IOException
     */
    public void destroy() throws ServletException, IOException {

        if (checkAdmin()) {
        // CSRF対策 tokenのチェック
        if (checkToken()) {

            // idを条件に従業員データを論理削除する
            service.destroy(toNumber(getRequestParam(AttributeConst.EMP_ID)));

            // セッションに削除完了のフラッシュメッセージを設定
            putSessionScope(AttributeConst.FLUSH, MessageConst.I_DELETED.getMessage());

            // 一覧画面にリダイレクト
            redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);
        }
    }
    }
        
    /**
     * 入力された従業員番号、パスワードの照合を行い、認証結果を返却する
     * @throws ServletException
     * @throws IOException
     */
    public void login() throws ServletException, IOException {

        String code = getRequestParam(AttributeConst.EMP_CODE);
        String plainPass = getRequestParam(AttributeConst.EMP_PASS);
        String pepper = getContextScope(PropertyConst.PEPPER);

        // 認証結果を取得
        boolean isValidEmployee = service.validateLogin(code, plainPass, pepper);

        if (isValidEmployee) {
            // 認証成功の場合

            // CSRF対策用トークンを設定
            putSessionScope(AttributeConst.TOKEN, getTokenId());

            // ログインした従業員のDBデータを取得
            EmployeeView ev = service.findOne(code, plainPass, pepper);
            putSessionScope(AttributeConst.LOGIN_EMP, ev);

            // セッションにログイン完了のフラッシュメッセージを設定
            putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGGEDIN.getMessage());

            // 一覧画面にリダイレクト
            redirect(ForwardConst.ACT_EMP, ForwardConst.CMD_INDEX);

        } else {
            // 認証失敗の場合

            // 認証失敗エラーメッセージ表示フラグを立てる
            putRequestScope(AttributeConst.LOGIN_ERR, true);

            // 入力された従業員番号を設定
            putRequestScope(AttributeConst.EMP_CODE, code);

            // ログイン画面を表示
            forward(ForwardConst.FW_LOGIN);
        }
    }

    /**
     * ログアウト処理を行う
     * @throws ServletException
     * @throws IOException
     */
    public void logout() throws ServletException, IOException {

        // セッションからログイン従業員のパラメータを削除
        removeSessionScope(AttributeConst.LOGIN_EMP);

        // セッションにログアウト時のフラッシュメッセージを設定
        putSessionScope(AttributeConst.FLUSH, MessageConst.I_LOGOUT.getMessage());

        // ログイン画面にリダイレクト
        redirect(ForwardConst.ACT_AUTH, ForwardConst.CMD_SHOW_LOGIN);
    }

/**
 * ログイン中の従業員が管理者かどうかチェックし、管理者でない場合はエラー画面を表示
 * @return true: 管理者 false: 管理者ではない
 * @throws ServletException
 * @throws IOException
 */
private boolean checkAdmin() throws ServletException, IOException {

    // セッションからログイン中の従業員情報を取得
    EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

    // 管理者でなければエラー画面を表示
    if (ev.getAdminFlag() != AttributeConst.ROLE_ADMIN.getIntegerValue()) {

        forward(ForwardConst.FW_ERR_UNKNOWN);
        return false;
    } else {

        return true;
    }
}
}