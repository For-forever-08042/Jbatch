package jp.co.mcc.nttdata.batch.business.com.bt_aplcom;

/**
 * バッチＡＰ業務共通ヘッダファイル
 *
 */
public class BT_aplcomService {

    public static final String C_ORACONN_MD = "MD";        /* Oracle接続先 MD(顧客管理)          */
    public static final String C_ORACONN_SD = "SD";        /* Oracle接続先 SD(顧客制度)          */
    public static final String C_ORACONN_BD = "BD";        /* Oracle接続先 BD(XXXXXXXX)          */
    public static final String C_ORACONN_HD = "HD";        /* Oracle接続先 HD(明細管理)          */

    /* 2022/09/22 MCCM初版 MOD START */
    /*#define C_BOTTOM_RANK         1*/         /* 最下位ランクコード                 */
    public static final int C_BOTTOM_RANK = 0;         /* 最下位ランクコード                 */
    /* 2022/09/22 MCCM初版 MOD END */
    public static final int C_ORA_BUSY_NOWAIT = -54;         /* ORACLE NOWAIT エラー       sqlcode */
    public static final int C_KIGYO_CD_CFH = 1010;         /* 顧客企業コード（CFH）              */
    public static final int C_KIGYO_CD_EC = 1020;         /* 顧客企業コード（EC）               */
    public static final int C_KIGYO_CD_KODAMA = 1030;         /* 顧客企業コード（コダマ）           */
    public static final int C_KIGYO_CD_MOBILE = 1040;         /* 顧客企業コード（モバイル）         */
    public static final int C_KIGYO_CD_EBINA = 1060;         /* 顧客企業コード（えびな)            */
    public static final int C_PUNCH_ERRORNO1 = 8010;         /* パンチエラー番号（会員番号エラー） */
    public static final int C_PUNCH_ERRORNO2 = 8020;         /* パンチエラー番号（顧客情報エラー） */
    public static final int C_PUNCH_ERRORNO3 = 8030;         /* パンチエラー番号（重複エラー）     */
    public static final int C_PUNCH_ERRORNO4 = 8040;         /* パンチエラー番号（店番号エラー）   */
    public static final int C_PUNCH_ERRORNO5 = 8050;         /* パンチエラー番号（退会済みエラー） */
    public static final int C_PUNCH_ERRORNO6 = 8060;         /* パンチエラー番号（仮退会済みエラー） */
    public static final int C_PUNCH_ERRORNO7 = 8070;         /* パンチエラー番号（MK顧客番号エラー） */
    public static final int C_PUNCH_ERRORNO8 = 8080;         /* パンチエラー番号（MK管理会員番号エラー）*/
    public static final int C_PUNCH_OK = 0;         /* パンチエラー番号（正常）           */
    /* 止め区分                                                                   */
    public static final int C_SEND_OK_3000 = 3000;         /* 送付可                             */
    public static final int C_SEND_NG_3001 = 3001;         /* 送付不可（ＤＭ未達：住所等異常）   */
    public static final int C_SEND_NG_3007 = 3007;         /* 送付不可（その他理由）             */
    public static final int C_SEND_NG_3031 = 3031;         /* 送付不可（顧客依頼停止）           */
    public static final int C_SEND_NG_3092 = 3092;         /* 送付不可（退会済み）               */
    public static final int C_SEND_NG_3093 = 3093;         /* 送付不可（対象外カード）           */
    public static final int C_SEND_NG_3099 = 3099;         /* 送付不可（未設定の為）             */
    public static final int C_SEND_OK_5000 = 5000;         /* 送信可                             */
    public static final int C_SEND_NG_5001 = 5001;         /* 送信不可（メール未達：アドレス異常）*/
    public static final int C_SEND_NG_5007 = 5007;         /* 送信不可（その他理由）             */
    public static final int C_SEND_NG_5031 = 5031;         /* 送信不可（顧客依頼停止）           */
    public static final int C_SEND_NG_5092 = 5092;         /* 送信不可（退会済み）               */
    public static final int C_SEND_NG_5093 = 5093;         /* 送信不可（対象外カード）           */
    public static final int C_SEND_NG_5099 = 5099;         /* 送信不可（未設定の為）             */

    public static final String LF = "\n";
    public static final String CRLF = "\r\n";
}
