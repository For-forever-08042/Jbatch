package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/
/*   名前： ホスト変数構造体 MS顧客制度情報 定義ファイル                      */
/*                 C_MS_KOKYAKU_SEDO_INFO_DATA.h                              */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/10/11 ISS 越後谷 ： 初版                                   */
/*      2.00 :2014/06/26 SSI 吉田   ： 資生堂ワタシプラス連携対応             */
/*                                    「提携先紐付登録年月日１」項目追加      */
/*                                    「提携先紐付解除年月日１」項目追加      */
/*      3.00 :2021/11/19 SSI 上野   ：「アプリ会員フラグ」項目追加            */
/*     40.00 :2022/09/05 SSI.岩井   ： MCCM初版                               */
/*     41.00 :2023/05/24 SSI.石     ： MCCMPH2                                */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MS顧客制度情報構造体 */
public class MS_KOKYAKU_SEDO_INFO_TBL  extends TBLBaseDto {

    public ItemDto kokyaku_no = new ItemDto(15+1);              ; /* 顧客番号              PK */
    public ItemDto tanjo_m = new ItemDto();                      ; /* 誕生月                   */
    public ItemDto entry = new ItemDto();                         ; /* エントリー               */
    public ItemDto senior = new ItemDto();                       ; /* シニア                   */
    public ItemDto nenji_rank_cd_0 = new ItemDto();               ; /* 年次ランクコード０       */
    public ItemDto nenji_rank_cd_1 = new ItemDto();               ; /* 年次ランクコード１       */
    public ItemDto  nenji_rank_cd_2 = new ItemDto();               ; /* 年次ランクコード２       */
    public ItemDto  nenji_rank_cd_3 = new ItemDto();               ; /* 年次ランクコード３       */
    public ItemDto  nenji_rank_cd_4 = new ItemDto();               ; /* 年次ランクコード４       */
    public ItemDto  nenji_rank_cd_5 = new ItemDto();               ; /* 年次ランクコード５       */
    public ItemDto  nenji_rank_cd_6 = new ItemDto();               ; /* 年次ランクコード６       */
    public ItemDto  nenji_rank_cd_7 = new ItemDto();               ; /* 年次ランクコード７       */
    public ItemDto  nenji_rank_cd_8 = new ItemDto();               ; /* 年次ランクコード８       */
    public ItemDto  nenji_rank_cd_9 = new ItemDto();               ; /* 年次ランクコード９       */
    public ItemDto  getuji_rank_cd_001 = new ItemDto();            ; /* 月次ランクコード００１   */
    public ItemDto  getuji_rank_cd_002 = new ItemDto();            ; /* 月次ランクコード００２   */
    public ItemDto  getuji_rank_cd_003 = new ItemDto();            ; /* 月次ランクコード００３   */
    public ItemDto  getuji_rank_cd_004 = new ItemDto();            ; /* 月次ランクコード００４   */
    public ItemDto  getuji_rank_cd_005 = new ItemDto();            ; /* 月次ランクコード００５   */
    public ItemDto  getuji_rank_cd_006 = new ItemDto();            ; /* 月次ランクコード００６   */
    public ItemDto  getuji_rank_cd_007 = new ItemDto();            ; /* 月次ランクコード００７   */
    public ItemDto  getuji_rank_cd_008 = new ItemDto();            ; /* 月次ランクコード００８   */
    public ItemDto  getuji_rank_cd_009 = new ItemDto();            ; /* 月次ランクコード００９   */
    public ItemDto  getuji_rank_cd_010 = new ItemDto();            ; /* 月次ランクコード０１０   */
    public ItemDto  getuji_rank_cd_011 = new ItemDto();            ; /* 月次ランクコード０１１   */
    public ItemDto  getuji_rank_cd_012 = new ItemDto();            ; /* 月次ランクコード０１２   */
    public ItemDto  getuji_rank_cd_101 = new ItemDto();            ; /* 月次ランクコード１０１   */
    public ItemDto  getuji_rank_cd_102 = new ItemDto();            ; /* 月次ランクコード１０２   */
    public ItemDto  getuji_rank_cd_103 = new ItemDto();            ; /* 月次ランクコード１０３   */
    public ItemDto  getuji_rank_cd_104 = new ItemDto();            ; /* 月次ランクコード１０４   */
    public ItemDto  getuji_rank_cd_105 = new ItemDto();            ; /* 月次ランクコード１０５   */
    public ItemDto  getuji_rank_cd_106 = new ItemDto();            ; /* 月次ランクコード１０６   */
    public ItemDto  getuji_rank_cd_107 = new ItemDto();            ; /* 月次ランクコード１０７   */
    public ItemDto  getuji_rank_cd_108 = new ItemDto();            ; /* 月次ランクコード１０８   */
    public ItemDto  getuji_rank_cd_109 = new ItemDto();            ; /* 月次ランクコード１０９   */
    public ItemDto  getuji_rank_cd_110 = new ItemDto();            ; /* 月次ランクコード１１０   */
    public ItemDto  getuji_rank_cd_111 = new ItemDto();            ; /* 月次ランクコード１１１   */
    public ItemDto  getuji_rank_cd_112 = new ItemDto();            ; /* 月次ランクコード１１２   */
    public ItemDto  circle_id_1 = new ItemDto();                   ; /* サークルＩＤ１           */
    public ItemDto  circle_id_2 = new ItemDto();                   ; /* サークルＩＤ２           */
    public ItemDto  circle_id_3 = new ItemDto();                   ; /* サークルＩＤ３           */
    public ItemDto  circle_id_4 = new ItemDto();                   ; /* サークルＩＤ４           */
    public ItemDto  circle_id_5 = new ItemDto();                   ; /* サークルＩＤ５           */
    public ItemDto  zaiseki_kaishi_ym = new ItemDto();             ; /* 在籍開始年月             */
    public ItemDto  shussan_coupon_hakko_flg_1 = new ItemDto();    ; /* 出産クーポン発行可否１   */
    public ItemDto  shussan_coupon_hakko_flg_2 = new ItemDto();    ; /* 出産クーポン発行可否２   */
    public ItemDto  shussan_coupon_hakko_flg_3 = new ItemDto();    ; /* 出産クーポン発行可否３   */
    public ItemDto  shain_kbn = new ItemDto();                     ; /* 社員区分                 */
    public ItemDto  portal_kaiin_flg = new ItemDto();              ; /* ポータル会員フラグ       */
    public ItemDto  ec_kaiin_flg = new ItemDto();                  ; /* ＥＣ会員フラグ           */
    public ItemDto  mobile_kaiin_flg = new ItemDto();              ; /* モバイル会員フラグ       */
    public ItemDto  denwa_no_toroku_flg = new ItemDto();           ; /* 電話番号登録フラグ       */
    public ItemDto  setai_torikomizumi_flg = new ItemDto();        ; /* 静態取込済みフラグ       */
    public ItemDto kazoku_id= new ItemDto(10+1);               ; /* 家族ＩＤ                 */
    public ItemDto  flg_1 = new ItemDto();                         ; /* フラグ１                 */
    public ItemDto  flg_2 = new ItemDto();                         ; /* フラグ２                 */
    public ItemDto  flg_3 = new ItemDto();                         ; /* フラグ３                 */
    public ItemDto  flg_4 = new ItemDto();                         ; /* フラグ４                 */
    public ItemDto  flg_5 = new ItemDto();                         ; /* フラグ５                 */
    public ItemDto  sagyo_kigyo_cd = new ItemDto();                ; /* 作業企業コード           */
    public ItemDto  sagyosha_id = new ItemDto();                   ; /* 作業者ＩＤ               */
    public ItemDto  sagyo_ymd = new ItemDto();                     ; /* 作業年月日               */
    public ItemDto  sagyo_hms = new ItemDto();                     ; /* 作業時刻                 */
    public ItemDto  batch_koshin_ymd = new ItemDto();              ; /* バッチ更新日             */
    public ItemDto  saishu_koshin_ymd = new ItemDto();             ; /* 最終更新日               */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();          ; /* 最終更新日時             */
    public ItemDto saishu_koshin_programid= new ItemDto(20+1); ; /* 最終更新プログラムＩＤ   */
    public ItemDto  himoduke_reg_ymd1 = new ItemDto();             ; /* 提携先紐付登録年月日１   */
    public ItemDto  himoduke_urg_ymd1 = new ItemDto();             ; /* 提携先紐付解除年月日１   */
    public ItemDto  ap_kaiin_flg = new ItemDto();                  ; /* アプリ会員フラグ         */
    public ItemDto  kokyaku_status = new ItemDto();                ; /* 顧客ステータス           */
    public ItemDto  kokoku_haishin_kyodaku_flg = new ItemDto();    ; /* 広告配信許諾フラグ       */
    public ItemDto  kokoku_haishin_kyodaku_flg_ymd = new ItemDto(); ; /* 広告配信許諾フラグ登録日 */
    public ItemDto   kokoku_haishin_kyodaku_flg_ymdhms = new ItemDto(); ; /* 広告配信許諾フラグ更新日時 */
    public ItemDto  kaiin_shikaku_kbn = new ItemDto();             ; /* 会員資格区分             */
    public ItemDto  keiyaku_kakutei_ymd = new ItemDto();           ; /* 契約確定年月日           */
    public ItemDto keiyaku_kanri_syubetsu= new ItemDto(5+1);   ; /* 契約管理種別             */
    public ItemDto  credit_kibo_flg = new ItemDto();               ; /* クレジット希望フラグ     */
    public ItemDto global_kaiin_flg= new ItemDto(3+1);         ; /* グローバル会員フラグ     */
    public ItemDto global_kaiin_koku_cd= new ItemDto(3+1);     ; /* グローバル会員国コード   */
    public ItemDto jikokuyo_kaiin_no= new ItemDto(18+1);       ; /* 自国用会員番号           */
    public ItemDto  line_connect_jokyo = new ItemDto();            ; /* ＬＩＮＥコネクト状況     */
    public ItemDto  line_connect_jokyo_ymd = new ItemDto();        ; /* ＬＩＮＥコネクト状況登録日 */
    public ItemDto  line_connect_jokyo_ymdhms = new ItemDto();     ; /* ＬＩＮＥコネクト状況更新日時 */
    public ItemDto  mcc_seido_kyodaku_flg = new ItemDto();         ; /* ＭＣＣ制度許諾フラグ     */
    public ItemDto  mcc_seido_kyodaku_koshinsha = new ItemDto();   ; /* ＭＣＣ制度許諾更新者     */
    public ItemDto  mcc_seido_kyodaku_koshin_ymdhms = new ItemDto(); ; /* ＭＣＣ制度許諾更新日時 */
    public ItemDto  corporate_kaiin_flg = new ItemDto();           ; /* コーポレート会員フラグ   */
    public ItemDto  corporate_kaiin_status = new ItemDto();        ; /* コーポレート会員ステータス */
    public ItemDto  corporate_kaiin_toroku_ymd = new ItemDto();    ; /* コーポレート会員登録日   */
    public ItemDto  zokusei_kanri_shutai_system = new ItemDto();   ; /* 属性管理主体システム     */
    /* 2023/05/24 MCCMPH2 ADD START */
    public ItemDto  sampling_yohi_flg = new ItemDto();             ; /* サンプリング要否フラグ   */
    public ItemDto  corporate_id_sentaku_mail_shubetsu = new ItemDto(); ; /* コーポレートＩＤ選択メール種別 */
    public ItemDto  dejitalu_ec_kaiin_nyukai_flg = new ItemDto();             ; /* デジタル会員ＥＣ入会フラグ   */
    public ItemDto  dejitalu_ec_kaiin_nyukai_ymdhms = new ItemDto();          ; /* デジタル会員ＥＣ入会更新日時   */
    public ItemDto  dejitalu_ap_kaiin_nyukai_flg = new ItemDto();             ; /* デジタル会員アプリ入会フラグ   */
    public ItemDto  dejitalu_ap_kaiin_nyukai_ymdhms = new ItemDto();          ; /* デジタル会員アプリ入会更新日時   */
    public ItemDto  kyu_nyukai_ymd = new ItemDto();             ; /* 旧入会年月日   */
    /* 2023/05/24 MCCMPH2 ADD END */

}
