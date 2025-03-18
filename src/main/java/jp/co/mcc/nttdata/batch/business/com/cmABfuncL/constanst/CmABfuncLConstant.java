package jp.co.mcc.nttdata.batch.business.com.cmABfuncL.constanst;

public class CmABfuncLConstant {
    /*----------------------------------------------------------------------------*/
    /*  トレース出力要否設定（0:不要、1:必要）                                */
    /*----------------------------------------------------------------------------*/
    /*      関数単位にトレース出力要否が設定できるように定義                      */

    public static int dbg_getaplogfmt = 0;     /* ＡＰログフォーマット取得処理           */
    public static int dbg_aplogwrite = 0;     /* ＡＰログ出力処理                       */
    public static int dbg_convut2sj = 0;     /* ＵＴＦ８ＳＪＩＳ変換処理               */
    public static int dbg_convsj2ut = 0;     /* ＳＪＩＳＵＴＦ８変換処理               */
    public static int dbg_oradbconnect = 1;     /* ＤＢ接続                               */
    public static int dbg_getbatdate = 0;     /* バッチ日付取得                         */
    public static int dbg_getsysdatetime = 0;     /* システム日付取得                       */
    public static int dbg_strcntutf8 = 0;     /* ＵＴＦ８文字列の長さを取得             */
    public static int dbg_kdatalock = 0;     /* 顧客データロック処理                   */
    public static int dbg_gettenaurbcd = 0;     /* テナント売場コード取得                 */
    public static int dbg_getentpoint = 0;     /* エントリーポイント取得                 */
    public static int dbg_changetkstatus = 1;     /* 提携ステータス変更                     */
    public static int dbg_getpostbarcode = 1;     /* 郵便番号コード変換                     */
    public static int dbg_GetTanCDV = 1;     /* 担当者コードＣＤＶ取得処理             */
    public static int dbg_GetPidCDV = 1;     /* 会員コード、顧客コード CDV取得処理     */
    public static int dbg_ConvTelNo = 1;     /* 電話番号ハイフンなし変換処理           */
    public static int dbg_CountAge = 1;     /* 年齢計算処理                           */
    public static int dbg_EncOrDec = 1;     /* 暗号化・復号化処理                     */
    public static int dbg_getpgname = 1;     /* プログラム名取得                       */
    public static int dbg_convhalf2full = 1;     /* 半角→全角変換処理                     */
    public static int dbg_convfull2half = 1;     /* 全角→半角変換処理                     */
    public static int dbg_setfullspace = 1;     /* 全角スペースセット処理                 */
    public static int dbg_GetSaveKkn = 1;     /* 保存期間取得処理                       */
    /* 2022/09/14 MCCM初版 ADD START */
    public static int dbg_GetPrefecturesCode = 1;     /* 都道府県コード取得処理                 */
    public static int dbg_GetPrefectures = 1;     /* 都道府県名取得処理処理                 */
    /* 2022/09/14 MCCM初版 ADD END */
    /* 2022/10/25 MCCM初版 ADD START */
    public static int dbg_GetTanCDV12 = 1;     /* 担当者コードＣＤＶ取得処理             */
    public static int dbg_GetPidCDV12 = 1;     /* 会員コード、顧客コード CDV取得処理     */
    public static int dbg_CorrectMemberNo = 1;     /* 会員番号補正処理     */

    public static int APLOG_MSG_MAX = 16; /* APログメッセージテーブルの件数 */

    public static int APLW_INPUT_MAX = 11550; /* 128桁×30行 ＵＴＦ対応で３倍にする +\n*30     */

    public static int CF_CORPID = 1;           /* ココカラファインヘルスケア企業コード */


}
