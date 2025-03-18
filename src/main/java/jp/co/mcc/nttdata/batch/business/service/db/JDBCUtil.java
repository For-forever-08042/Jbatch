/**********************************************************************************
 * Created         : 2010/10/12
 * Copyright(C)    : Copyright c 2012 NTT DATA CORPORATION
 * Original Author : ssi
 *
 *---------------------------------------------------------------------------------
 * MODIFICATION HISTORY
 *---------------------------------------------------------------------------------
 * When        Who     Version   Why
 *---------------------------------------------------------------------------------
 * 2010/10/12  ssi   1.00      Created
 *
 **********************************************************************************/
package jp.co.mcc.nttdata.batch.business.service.db;

import jp.co.mcc.nttdata.batch.fw.com.constants.KshScriptTypes;
import jp.co.mcc.nttdata.batch.fw.util.TaskResultUtil;
import jp.co.mcc.nttdata.cmAOclibJ.database.CMConnectionManager;
import jp.co.mcc.nttdata.cmAOclibJ.exception.CMAOException;
import jp.co.mcc.nttdata.cmAOclibJ.log.ap.CMAOAPLogMsgConst;
import jp.co.mcc.nttdata.cmAOclibJ.log.ap.CMAOApLogWriter;
import jp.co.mcc.nttdata.cmAOclibJ.log.ap.CMAOLogRegistType;
import jp.co.mcc.nttdata.cmAOclibJ.log.trace.TraceFileWriterForJBat;
import jp.co.mcc.nttdata.cmAOclibJ.settings.CMAODatabaseSettings;
import jp.co.mcc.nttdata.cmAOclibJ.util.CMAOConst;
import jp.co.mcc.nttdata.cmAOclibJ.util.CMAOLibUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

/**
 * クラス名　：cmBTbmecメインクラス
 * クラス説明：売上明細取込用メインクラス
 *
 * @author ssi
 * @version 1.00
 */
@Component
public class JDBCUtil {

    /**
     * 起動パラメータ：数
     */
    private static final int ARG_COUNT = 3;
    /**
     * 起動パラメータ：パラメータファイル フルパス
     */
    private static final int ARG_INDEX_PFILE = 0;
    /**
     * 起動パラメータ：トレースオプション
     */
    private static final int ARG_INDEX_TRACE = 1;
    /**
     * トレース出力フラグ
     */
    private static boolean isTrace = false;
    /**
     * バージョン出力フラグ
     */
    private static boolean isVersion = false;

    Connection conn = null;

    TraceFileWriterForJBat trace = null;
    String configureFile = "/config/cmBTbmeeP040";


    public void doBefore(String[] args) {

        String businessId = args[0];

//        try {
//            // APログ出力準備
//            CMAOApLogWriter.loadConstantMessage(new CMAOAPLogMsgConst() {
//                @Override
//                protected HashMap<String, String> getGyomuOrginalConstMsgList() {
//                    return null;
//                }
//            });
//            CMAOApLogWriter.setSystemID(businessId);
//
//            // バイナリモジュール開始ログ出力
//            CMAOApLogWriter.writeStartLog(KshScriptTypes.valueOf(businessId).comment);

            // パラメータチェック
//            checkParam(businessId, args);

            // バージョン出力
//            if (isVersion) {
//                final String version = businessId + " version \"" + KshScriptTypes.valueOf(businessId).programmeVersion + "\"";
//                CMAOApLogWriter.writeApLog(trace, CMAOAPLogMsgConst.INFO_ANY, CMAOLogRegistType.COMMON, version);
//                return;
//            }

            // トレースログ準備
//            if (isTrace) {
//                trace = new TraceFileWriterForJBat(businessId, isTracable(args[ARG_INDEX_TRACE]));
//            } else {
//                trace = new TraceFileWriterForJBat(businessId, false);
//            }
//            trace.start();
//
//            trace.info("#実施前#####################");
//            trace.info(getMemoryInfo());
//
//            // 起動パラメータをトレースログへ出力
//            trace.traceExecutionParams(args);

//        } catch (CMAOException e) {
//            // トレースログ出力
//            if (trace != null) {
//                trace.error("APエラー発生", e);
//            }
//
//        } catch (Exception e) {
//            // トレースログ出力
//            if (trace != null) {
//                trace.error("エラー発生", e);
//            }
//            // APログ出力
//            CMAOApLogWriter.writeApLog4Err(trace, e);
//
//        }
    }

    public void doAfter(boolean isNormalEnd) {
        try {
            // アプリケーション戻り値を設定・終了

            System.exit(TaskResultUtil.RESULT_CODE);
//            if (isNormalEnd) {
//            } else {
//                System.exit(CMAOConst.RETURN_NG);
//            }
        } finally {
            // DB切断
            CMConnectionManager.getInstance().closeConnection(conn, trace);

//            trace.info("#実施後#####################");
//            trace.info(getMemoryInfo());
//
//            if (trace != null) {
//                trace.end();
//                trace.destroy();
//            }
        }
    }

    public synchronized void commit(KshScriptTypes kshScriptTypes) {
        CMConnectionManager.getInstance().commit(conn, trace);
        // 正常終了ログ出力
        CMAOApLogWriter.writeNormalEndLog(kshScriptTypes.programmeId);
    }

    public synchronized Connection getConnection(CMAODatabaseSettings databaseSettings) throws CMAOException, SQLException {
        if (conn == null) {
            // DB接続
            CMConnectionManager.getInstance().initialize(databaseSettings, trace);
            //コネクション取得
            conn = CMConnectionManager.getInstance().getConnection(trace);

            //オートコミットOFF
            conn.setAutoCommit(false);

            return conn;
        } else {
            return conn;
        }
    }

    public synchronized Connection getConnection() throws CMAOException, SQLException {

        return getConnection(loadDatabaseSettings());
    }
    public CMAODatabaseSettings loadDatabaseSettings() throws CMAOException {
        return new CMAODatabaseSettings(loadProperties(), trace);
    }
    public Properties loadProperties() throws CMAOException {

//        trace.start();

        Properties prop = new Properties();

        try {
            ClassPathResource resource = new ClassPathResource(configureFile);
            InputStream stream = resource.getInputStream();
            prop.loadFromXML(stream);
            stream.close();

        } catch (IOException e) {
            // ファイルオープンエラー
            StringBuffer buf = new StringBuffer();
            buf.append("ファイルを開けません。")
                    .append(CMAOConst.NL)
                    .append(configureFile);
//            ERR :ファイル入出力エラー(%s=エラーになった理由)
            throw new CMAOException(e, CMAOAPLogMsgConst.ERR_FILEIO, CMAOLogRegistType.COMMON, buf.toString());
        }

//        trace.end();
        return prop;
    }

    /**
     * メソッド名　：起動パラメータチェック
     * メソッド説明：起動パラメータの数・トレースオプション指定値のチェック
     * 備考　　　　：当メソッド後にトレースログ出力を開始するため、ここではトレースログは出力しない
     *
     * @param args 起動パラメータ
     * @throws CMAOException 起動パラメータ不正
     */
    private static void checkParam(String id, String[] args) throws CMAOException {
        if (ARG_COUNT < args.length) {
            throw new CMAOException("CMBTbmecLogMsgConst.ERR_PARAMETER", id, CMAOLogRegistType.COMMON, "引数の数が不正です。");
        }
        for (int i = 1; i < args.length; i++) {
            if (args[i].matches("[Oo][Nn]")) {
                isTrace = true;
            } else if (args[i].matches("-[Vv][Ee][Rr][Ss][Ii][Oo][Nn]")) {
                isVersion = true;
            }
        }
    }

    /**
     * メソッド名　：トレース出力可否判定
     * メソッド説明：
     *
     * @param traceSwitch 出力切替スイッチ ONまたはonでトレース可
     * @return true.トレース可
     */
    private static boolean isTracable(String traceSwitch) {
        if (CMAOLibUtil.isNotEmpty(traceSwitch)) {
            if (traceSwitch.matches("[Oo][Nn]")) {
                return true;
            }
        }
        return false;
    }

    public static String getMemoryInfo() {
        DecimalFormat f1 = new DecimalFormat("#,###KB");
        DecimalFormat f2 = new DecimalFormat("##.#");

        long free = Runtime.getRuntime().freeMemory() / 1024;
        long total = Runtime.getRuntime().totalMemory() / 1024;
        long max = Runtime.getRuntime().maxMemory() / 1024;
        long used = total - free;

        double ratio = (used * 100 / (double) total);

        String info = "Java MemoryInfo :" + System.getProperty("line.separator")
                + "Total = " + f1.format(total) + System.getProperty("line.separator")
                + "Used = " + f1.format(used) + " (" + f2.format(ratio) + "%)" + System.getProperty("line.separator")
                + "Max = " + f1.format(max);

        return info;
    }


    /**
     * メソッド名：カレンダーオブジェクト取得処理
     * メソッド説明：指定された日付文字列より、指定日付のカレンダーオブジェクトを生成し返却します。
     * 備考：
     *
     * @param date 日付文字列（YYYYMMDD)
     * @return カレンダーオブジェクト
     */
    public Calendar createCalendarObject(String date) {
        Calendar calObj = Calendar.getInstance();
        calObj.clear();
        int year = Integer.parseInt((CMAOLibUtil.zeroPadding(date, 8)).substring(0, 4));
        int month = Integer.parseInt((CMAOLibUtil.zeroPadding(date, 8)).substring(4, 6)) - 1;
        int day = Integer.parseInt((CMAOLibUtil.zeroPadding(date, 8)).substring(6, 8));

        calObj.set(year, month, day);
        return calObj;
    }

    /**
     * メソッド名　：ResultSetからString型を取得
     *
     * @param rset    ResultSet
     * @param keyName キー名称
     * @return 結果文字列
     * @throws SQLException SQLException
     */
    private String getNullToEmptyString(ResultSet rset, String keyName) throws SQLException {
        return CMAOLibUtil.nullToEmpty(rset.getString(keyName)).trim();
    }

    /**
     * メソッド名　：ResultSetからString型を取得
     *
     * @param rset    ResultSet
     * @param keyName キー名称
     * @return 結果文字列
     * @throws SQLException SQLException
     */
    private BigDecimal getNullToZero(ResultSet rset, String keyName) throws SQLException {
        return getNullToZero(rset.getBigDecimal(keyName));
    }

    /**
     * メソッド名　：BigDecimalからString型を変換
     *
     * @param target オブジェクト
     * @return 結果文字列
     */
    private String getNullToEmptyString(BigDecimal target) {
        if (target == null) {
            return "";
        }
        return target.toPlainString();
    }

    /**
     * メソッド名　：数値がnullの場合、0を返す
     *
     * @param target オブジェクト
     * @return 結果数値
     */
    private BigDecimal getNullToZero(BigDecimal target) {
        if (target == null) {
            return new BigDecimal(0);
        }
        return target;
    }

    /**
     * メソッド名：string2BigDecimal
     * メソッド説明：StringをBigDecimalへ型変換する。
     * 　　　　　　　CMAOLibUtil.string2BigDecimal とは異なり、NULL、空文字列、空白、数値でない文字列の場合は NULL を返す。
     *
     * @param target 変換対象文字列
     * @return 型変換した数値
     */
    private BigDecimal string2BigDecimal(String target) {
        // 明らかに数値でない場合は NULL
        if (CMAOLibUtil.isEmpty(target, true)) {
            return null;
        }
        if (!CMAOLibUtil.isNumeric(target)) {
            return null;
        }
        try {
            return new BigDecimal(target);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
