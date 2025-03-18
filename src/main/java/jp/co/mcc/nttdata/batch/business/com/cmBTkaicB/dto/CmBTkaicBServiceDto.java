package jp.co.mcc.nttdata.batch.business.com.cmBTkaicB.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;

public class CmBTkaicBServiceDto {

    /* 動的ＳＱＬ作成用 */
    ItemDto          str_sql = new ItemDto(12288)          ; /* 実行用SQL文字列                    */

    public KAIIN_INFO_FL     h_kaiin_info = new KAIIN_INFO_FL();              /* 出力ファイル用バッファ       */

    /* 処理用 */
    public int  date_today;               /* 当日日付                           */
    public int  date_yesterday;           /* バッチ処理日前日                   */

    /* 標識変数 */
    public ItemDto indi_goopon_no = new ItemDto();
    public ItemDto indi_goopon_kaiin_status = new ItemDto();
    public ItemDto indi_goopon_kaiin_sakujo_flg = new ItemDto();
    public ItemDto indi_seibetu_cd = new ItemDto();
    public ItemDto indi_kekkon_cd = new ItemDto();
    public ItemDto indi_shokugyo_cd = new ItemDto();
    public ItemDto indi_sei_ymd = new ItemDto();
    public ItemDto indi_yubin_no_header = new ItemDto();
    public ItemDto indi_yubin_no_body = new ItemDto();
    public ItemDto indi_todofuken_cd = new ItemDto();
    public ItemDto indi_toroku_ymd = new ItemDto();
    public ItemDto indi_koshin_ymdhms = new ItemDto();
    public ItemDto indi_dm_fuyo_flg = new ItemDto();
    public ItemDto indi_nyukai_tempo_cd = new ItemDto();
    public ItemDto indi_dm_atesaki_fumei_flg = new ItemDto();
    public ItemDto indi_mk_nyukai_moshikomi_ymd = new ItemDto();
    public ItemDto indi_md_kigyo_cd = new ItemDto();
    public ItemDto indi_zokusei_flg = new ItemDto();
    public ItemDto indi_group_kaiin_kbn = new ItemDto();
    public ItemDto indi_group_kaiin_toroku_ymd = new ItemDto();
    public ItemDto indi_group_kaiin_status = new ItemDto();
    public ItemDto indi_kaisha_mei = new ItemDto();
    public ItemDto indi_busho_mei = new ItemDto();
    public ItemDto indi_kanshin_bunya_cd = new ItemDto();
    public ItemDto indi_mail_haishin_error_flg = new ItemDto();
    public ItemDto indi_mail_haishin_kyodaku_flg = new ItemDto();
    public ItemDto indi_global_kaiin_kuni_cd = new ItemDto();
    public ItemDto indi_kokoku_flg = new ItemDto();
    public ItemDto indi_mcc_group_kaiin_kbn = new ItemDto();
    public ItemDto indi_mcc_group_kaiin_toroku_ymd = new ItemDto();

    public class KAIIN_INFO_FL{
        public ItemDto       goopon_no = new ItemDto(17);             /* グーポン番号                  */
        public ItemDto goopon_kaiin_status = new ItemDto();       /* GOOPON会員ステータス          */
        public ItemDto goopon_kaiin_sakujo_flg = new ItemDto();   /* GOOPON会員削除フラグ          */
        public ItemDto seibetu_cd = new ItemDto();                /* 性別コード                    */
        public ItemDto kekkon_cd = new ItemDto();                 /* 結婚コード                    */
        public ItemDto shokugyo_cd = new ItemDto();               /* 職業コード                    */
        public ItemDto          sei_ymd = new ItemDto(11);               /* 生年月日                      */
        public ItemDto       yubin_no_header = new ItemDto(4);        /* 郵便番号前3桁                 */
        public ItemDto       yubin_no_body = new ItemDto(5);          /* 郵便番号後4桁                 */
        public ItemDto       todofuken_cd = new ItemDto(3);              /* 都道府県コード                */
        public ItemDto       toroku_ymd = new ItemDto(11);            /* 登録日                        */
        public ItemDto       koshin_ymdhms = new ItemDto(21);         /* 更新日時                      */
        public ItemDto dm_fuyo_flg = new ItemDto();               /* DM要不要フラグ                */
        public ItemDto       nyukai_tempo_cd = new ItemDto(5);        /* 入会店舗コード                */
        public ItemDto dm_atesaki_fumei_flg = new ItemDto();      /* DM宛先不明フラグ              */
        public ItemDto       mk_nyukai_moshikomi_ymd = new ItemDto(11);  /* MK入会申込用紙記載日          */
        public ItemDto       md_kigyo_cd = new ItemDto(5);               /* MD企業コード                  */
        public ItemDto          zokusei_flg = new ItemDto(11);               /* 属性フラグ                    */
        public ItemDto group_kaiin_kbn = new ItemDto();           /* グループ会員区分              */
        public ItemDto       group_kaiin_toroku_ymd = new ItemDto(21);    /* グループ会員登録日            */
        public ItemDto       group_kaiin_status = new ItemDto(2);        /* グループ会員ステータス        */
        public ItemDto       kaisha_mei = new ItemDto(160);           /* 会社名                        */
        public ItemDto       busho_mei = new ItemDto(160);            /* 部署名                        */
        public ItemDto       kanshin_bunya_cd = new ItemDto(256);     /* 関心分野コード                */
        public ItemDto mail_haishin_error_flg = new ItemDto();    /* メール配信エラーフラグ        */
        public ItemDto mail_haishin_kyodaku_flg = new ItemDto();  /* メール配信許諾フラグ          */
        public ItemDto       global_kaiin_kuni_cd = new ItemDto(4);   /* グローバル会員国コード        */
        public ItemDto kokoku_flg = new ItemDto();                /* 広告許諾フラグ              */
        public ItemDto mcc_group_kaiin_kbn = new ItemDto();           /* MCCグループ会員区分              */
        public ItemDto       mcc_group_kaiin_toroku_ymd = new ItemDto(21);    /* MCCグループ会員登録日            */
    }
}



