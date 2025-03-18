package jp.co.mcc.nttdata.batch.business.service.db.dto.h;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.TBLBaseDto;

/******************************************************************************/
/*   名前： ホスト変数構造体 MM顧客属性情報 定義ファイル                      */
/*                 C_MM_KOKYAKU_ZOKUSE_INFO_DATA.h                            */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 5（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :2012/10/11 ISS 越後谷 ： 初版                                   */
/*      2.00 :2016/08/05 SSI 田頭   ： C-MAS対応 Eメールアドレス４の追加      */
/*     40.00 :2022/09/06 SSI.岩井   ： MCCM初版                               */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2012 NTT DATA CORPORATION                                   */

/******************************************************************************/

/*----------------------------------------------------------------------------*/
/*グローバル変数                                                              */
/*----------------------------------------------------------------------------*/
/* MM顧客属性情報構造体 */
public class MM_KOKYAKU_ZOKUSE_INFO_TBL  extends TBLBaseDto {

    public ItemDto kokyaku_no= new ItemDto(15+1);             /* 顧客番号              PK */
    public ItemDto kyumin_flg = new ItemDto();                   /* 休眠フラグ               */
    public ItemDto yubin_no= new ItemDto(10+1);               /* 郵便番号                 */
    public ItemDto yubin_no_cd= new ItemDto(23+1);            /* 郵便番号コード           */
    public ItemDto jusho_1= new ItemDto(10*3+1);              /* 住所１                   */
    public ItemDto jusho_2= new ItemDto(80*3+1);              /* 住所２                   */
    public ItemDto jusho_3= new ItemDto(80*3+1);              /* 住所３                   */
    public ItemDto denwa_no_1= new ItemDto(15+1);             /* 電話番号１               */
    public ItemDto denwa_no_2= new ItemDto(15+1);             /* 電話番号２               */
    public ItemDto kensaku_denwa_no_1= new ItemDto(15+1);     /* 検索電話番号１           */
    public ItemDto kensaku_denwa_no_2= new ItemDto(15+1);     /* 検索電話番号２           */
    public ItemDto email_address_1= new ItemDto(255+1);       /* Ｅメールアドレス１       */
    public ItemDto email_address_2= new ItemDto(100+1);       /* Ｅメールアドレス２       */
    public ItemDto denwa_no_3= new ItemDto(15+1);             /* 電話番号３               */
    public ItemDto denwa_no_4= new ItemDto(15+1);             /* 電話番号４               */
    public ItemDto kensaku_denwa_no_3= new ItemDto(15+1);     /* 検索電話番号３           */
    public ItemDto kensaku_denwa_no_4= new ItemDto(15+1);     /* 検索電話番号４           */
    public ItemDto shokugyo= new ItemDto(40*3+1);             /* 職業                     */
    public ItemDto kinmu_kbn = new ItemDto();                    /* 勤務区分                 */
    public ItemDto jitaku_jusho_cd= new ItemDto(11+1);        /* 自宅住所コード           */
    public ItemDto  sagyo_kigyo_cd = new ItemDto();               /* 作業企業コード           */
    public ItemDto  sagyosha_id = new ItemDto();                  /* 作業者ＩＤ               */
    public ItemDto  sagyo_ymd = new ItemDto();                    /* 作業年月日               */
    public ItemDto  sagyo_hms = new ItemDto();                    /* 作業時刻                 */
    public ItemDto  batch_koshin_ymd = new ItemDto();             /* バッチ更新日             */
    public ItemDto  saishu_koshin_ymd = new ItemDto();            /* 最終更新日               */
    public ItemDto  saishu_koshin_ymdhms = new ItemDto();         /* 最終更新日時             */
    public ItemDto saishu_koshin_programid= new ItemDto(20+1);  /* 最終更新プログラムＩＤ */
    public ItemDto email_address_3= new ItemDto(255+1);       /* Ｅメールアドレス３       */
    public ItemDto email_address_4= new ItemDto(100+1);       /* Ｅメールアドレス４       */
    public ItemDto  todofuken_cd = new ItemDto();                 /* 都道府県コード           */
    public ItemDto address= new ItemDto(200*3+1);             /* 住所                     */
    public ItemDto  x_zahyo_cd = new ItemDto();                   /* Ｘ座標コード             */
    public ItemDto  y_zahyo_cd = new ItemDto();                   /* Ｙ座標コード             */
    public ItemDto kaisha_name= new ItemDto(80*3+1);          /* 会社名                   */
    public ItemDto busho_name= new ItemDto(80*3+1);           /* 部署名                   */
    public ItemDto kanshin_bunya_cd= new ItemDto(256+1);      /* 関心分野コード           */

}
