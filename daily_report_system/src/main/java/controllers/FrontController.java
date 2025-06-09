package controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import actions.ActionBase;
import actions.UnknownAction;
import constants.ForwardConst;

/**
 * フロントコントローラー
 */
@WebServlet("/")
public class FrontController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public FrontController() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // パラメータに該当するActionクラスのインスタンスを作成
        ActionBase action = getAction(request, response);

        // サーブレットコンテキスト、リクエスト、レスポンスをActionクラスのフィールドに設定
        action.init(getServletContext(), request, response);

        // Actionクラスの処理を呼び出し
        action.process();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * リクエストパラメータの値に該当するActionクラスのインスタンスを作成し、返却する
     * (例: リクエストパラメータがaction=Employeeの場合、actions.EmployeeActionオブジェクト)
     * @param request リクエスト
     * @param response レスポンス
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" }) // コンパイラ警告抑制
    private ActionBase getAction(HttpServletRequest request, HttpServletResponse response) {
        Class type = null;
        ActionBase action = null;

        // リクエストからパラメータ"action"の値を取得 (例: Employee, Report)
        String actionString = request.getParameter(ForwardConst.ACT.getValue());

        // 該当するActionオブジェクトを作成 (例: リクエストからパラメータの値がEmployeeの場合、actions.EmployeeActionオブジェクト)
        try {
            // リクエストパラメータに該当する"actions.パッケージ名.Action"のクラスオブジェクトを取得
            type = Class.forName(String.format("actions.%sAction", actionString));

            // ActionBaseのオブジェクトとして新規クラスのインスタンスを生成
            action = (ActionBase) (type.asSubclass(ActionBase.class)
                    .getDeclaredConstructor()
                    .newInstance());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException
                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            // リクエストパラメータに該当する"action"のクラスが存在しない場合
            // (例: リクエストパラメータにaction=xxxxxと設定されているが、actions.xxxxxActionがない場合)
            // エラー処理を行うActionオブジェクトを作成
            action = new UnknownAction();
        }

        return action;
    }
}