package jp.co.mcc.nttdata.batch.business.service.db.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.service.db.JDBCUtil;
import jp.co.mcc.nttdata.batch.fw.com.dto.StatementItem;
import jp.co.mcc.nttdata.cmAOclibJ.database.CMConnectionManager;
import jp.co.mcc.nttdata.cmAOclibJ.exception.CMAOException;
import jp.co.mcc.nttdata.cmAOclibJ.log.ap.CMAOAPLogMsgConst;
import jp.co.mcc.nttdata.cmAOclibJ.log.ap.CMAOLogRegistType;
import jp.co.mcc.nttdata.cmAOclibJ.settings.CMAODatabaseSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Map;

@Service
public class BasicMapper {
    @Autowired
    JDBCUtil oracleDBUtil;

    public String formatSQL(String sql, String... param) {
        return String.format(sql, param);
    }

    public void connect(SqlstmDto sql) {
        try {
            CMAODatabaseSettings cmaoDatabaseSettings = oracleDBUtil.loadDatabaseSettings();
            cmaoDatabaseSettings.setUrl(String.format("jdbc:postgresql://%s:%s/%s", cmaoDatabaseSettings.getHost(), cmaoDatabaseSettings.getPort(), cmaoDatabaseSettings.getSid()));
            cmaoDatabaseSettings.setUser(sql.ORAUSR.arr);
            cmaoDatabaseSettings.setPasswd(sql.PASSWD.arr);
            cmaoDatabaseSettings.setSid(sql.ORASID.arr);
            CMConnectionManager.getInstance().initialize(cmaoDatabaseSettings, null);
            //コネクション取得
            Connection conn = CMConnectionManager.getInstance().getConnection(null);
            //オートコミットOFF
            conn.setAutoCommit(false);
            sql.conn = conn;
            sql.sql=new StringDto();
        } catch (SQLException e) {
            sql.sqlcode = e.getErrorCode();
//            throw new CMAOException(e, CMAOAPLogMsgConst.ERR_SQL, CMAOLogRegistType.COMMON, "PreparedStatement構築", e.getErrorCode(), "");
        } catch (CMAOException e) {
            if (e.getCause() instanceof SQLException) {
                sql.sqlcode = ((SQLException) e.getCause()).getErrorCode();
            } else {
                sql.sqlcode = -1;
            }
        }


    }


    public ResultSet executeQuery(Connection conn, String sql) throws CMAOException {


        // 起動時システム日付の取得を行うので、システム日付取得用クエリのみ最初に初期化する
        try {
            // システム日付取得クエリ
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.clearParameters();
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new CMAOException(e, CMAOAPLogMsgConst.ERR_SQL, CMAOLogRegistType.COMMON, "PreparedStatement構築", e.getErrorCode(), "");
        }
    }

    /**
     * メソッド名　：PreparedStatementの準備
     * メソッド説明：当業務で使用するPreparedStatementをプリコンパイル
     *
     * @throws CMAOException バッチアプリケーション例外
     */
    @Deprecated
    public ResultSet executeQuery(Connection conn, String sql, Map<Integer, StatementItem> param) throws CMAOException {


        // 起動時システム日付の取得を行うので、システム日付取得用クエリのみ最初に初期化する
        try {
            // システム日付取得クエリ
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.clearParameters();
            prepareParameter(preparedStatement, param);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new CMAOException(e, CMAOAPLogMsgConst.ERR_SQL, CMAOLogRegistType.COMMON, "PreparedStatement構築", e.getErrorCode(), "");
        }
    }

    public int executeUpdate(Connection conn, String sql, Map<Integer, StatementItem> param) throws CMAOException {

        try {
            // 起動時システム日付の取得を行うので、システム日付取得用クエリのみ最初に初期化する
            // システム日付取得クエリ
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.clearParameters();
            prepareParameter(preparedStatement, param);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new CMAOException(e, CMAOAPLogMsgConst.ERR_SQL, CMAOLogRegistType.COMMON, "PreparedStatement構築", e.getErrorCode(), "");
        }
    }

    public void prepareParameter(PreparedStatement preparedStatement, Map<Integer, StatementItem> param) throws SQLException {
        for (Map.Entry<Integer, StatementItem> item : param.entrySet()) {
            StatementItem paramItem = item.getValue();
            if (paramItem.getValue() == null) {
                preparedStatement.setNull(item.getKey(), paramItem.types);
                continue;
            }
            switch (paramItem.getClass().getSimpleName()) {
                case "String":
                    preparedStatement.setString(item.getKey(), (String) paramItem.value);
                    break;
                case "Integer":
                    preparedStatement.setInt(item.getKey(), (Integer) paramItem.value);
                    break;
                case "Double":
                    break;
                case "BigDecimal":
                    preparedStatement.setBigDecimal(item.getKey(), (BigDecimal) paramItem.value);
                    break;
                case "Date":
                    preparedStatement.setDate(item.getKey(), (Date) paramItem.value);
                    break;
                case "Timestamp":
                    preparedStatement.setTimestamp(item.getKey(), (Timestamp) paramItem.value);
                    break;
                //TODO
            }

        }

    }
}
