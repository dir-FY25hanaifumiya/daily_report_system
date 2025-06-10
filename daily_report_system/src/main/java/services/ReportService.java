package services;

import java.time.LocalDateTime;
import java.util.List;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import actions.views.ReportConverter;
import actions.views.ReportView;
import constants.JpaConst;
import models.Report;
import models.validators.ReportValidator;

/**
 * 日報データの操作に関わる処理を行うクラス
 */
public class ReportService extends ServiceBase {

    /**
     * 指定した従業員が作成した日報データの件数を、指定されたページ数の一覧画面に表示する分取得し、ReportViewのリストで返す
     * @param employee 従業員
     * @param page ページ数
     * @return 一覧画面に表示する日報データのリスト
     */
    public List<ReportView> getMinePage(EmployeeView employee, int page) {
        List<Report> reports = em.createNamedQuery(JpaConst.Q_REP_GET_ALL_MINE, Report.class)
            .setParameter(JpaConst.JPQL_PARM_EMPLOYEE, EmployeeConverter.toModel(employee))
            .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
            .setMaxResults(JpaConst.ROW_PER_PAGE)
            .getResultList();
        return ReportConverter.toViewList(reports);
    }

    /**
     * 指定した従業員が作成した日報データの件数を取得し、返す
     * @param employee 従業員
     * @return 日報データの件数
     */
    public long countAllMine(EmployeeView employee) {
        long count = (long) em.createNamedQuery(JpaConst.Q_REP_COUNT_ALL_MINE)
            .setParameter(JpaConst.JPQL_PARM_EMPLOYEE, EmployeeConverter.toModel(employee))
            .getSingleResult();
        return count;
    }

    /**
     * 指定されたページ数の一覧画面に表示する日報データを取得し、返す
     * @param page ページ数
     * @return 一覧画面に表示する日報データのリスト
     */
    public List<ReportView> getAllPerPage(int page) {
        List<Report> reports = em.createNamedQuery(JpaConst.Q_REP_GET_ALL, Report.class)
            .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
            .setMaxResults(JpaConst.ROW_PER_PAGE)
            .getResultList();
        return ReportConverter.toViewList(reports);
    }

    /**
     * 日報データの件数を取得し、返す
     * @return 日報データの件数
     */
    public long countAll() {
        long count = (long) em.createNamedQuery(JpaConst.Q_REP_COUNT)
            .getSingleResult();
        return count;
    }

    /**
     * 指定したidに紐づく日報データのインスタンスを取得する
     * @param id 日報データのid
     * @return 取得したデータのインスタンス
     */
    public ReportView findOne(int id) {
        return ReportConverter.toView(em.find(Report.class, id));
    }

    /**
     * 画面から入力された日報の登録内容を元に、日報データを作成する
     * @param rv 日報データのViewモデル
     * @return バリデーションで発生したエラーのリスト
     */
    public List<String> create(ReportView rv) {
        List<String> errors = ReportValidator.validate(rv);

        if (errors.size() == 0) {
            LocalDateTime ldt = LocalDateTime.now();
            rv.setCreatedAt(ldt);
            rv.setUpdatedAt(ldt);

            createInternal(rv);
        }

        // バリデーションで発生したエラーを返却（エラーがなければ空のリスト）
        return errors;
    }

    /**
     * 画面から入力された日報の編集内容を元に、日報データを更新する
     * @param rv 日報データのViewモデル
     * @return バリデーションで発生したエラーのリスト
     */
    public List<String> update(ReportView rv) {
        List<String> errors = ReportValidator.validate(rv);

        if (errors.size() == 0) {
            LocalDateTime ldt = LocalDateTime.now();
            rv.setUpdatedAt(ldt);

            updateInternal(rv);
        }

        // バリデーションで発生したエラーを返却（エラーがなければ空のリスト）
        return errors;
    }

    /**
     * いわゆる登録処理を行う
     * @param rv 日報データのViewインスタンス
     */
    private void createInternal(ReportView rv) {
        em.getTransaction().begin();
        em.persist(ReportConverter.toModel(rv));
        em.getTransaction().commit();
    }

    /**
     * 更新処理を行う
     * @param rv 日報データのViewインスタンス
     */
    private void updateInternal(ReportView rv) {
        em.getTransaction().begin();
        Report r = em.find(Report.class, rv.getId());
        ReportConverter.copyViewToModel(r, rv);
        em.getTransaction().commit();
    }
}