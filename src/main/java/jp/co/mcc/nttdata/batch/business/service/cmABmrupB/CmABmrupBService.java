package jp.co.mcc.nttdata.batch.business.service.cmABmrupB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmABmrupBService {

    public int  Chk_ArgtInf( StringDto Arg_in );  /* 引数チェック（-t） */
    public int  Chk_ArgdInf( StringDto Arg_in );  /* 引数チェック（-d） */
    public int  Chk_ArguInf( StringDto Arg_in );  /* 引数チェック（-u） */
    public int  Chk_ArgpInf( StringDto Arg_in );  /* 引数チェック（-p） */
    public int  Chk_ArgsInf( StringDto Arg_in );  /* 引数チェック（-s） */
    public int  Chk_ArgoInf( StringDto Arg_in );  /* 引数チェック（-o） */
    public int  OpenOutFile();               /* 出力ファイルオープン */
    public int  cmABmrupB_main();            /* 当日更新分反映主処理 */
    public int  UpdateKokyakuinfo_bk();      /* MM顧客情報_BK更新処理 */
    public int  UpdateKokyakuzokusei_bk();   /* MM顧客属性情報_BK更新処理 */
    public int  UpdateKokyakukigybetu_bk();  /* MM顧客企業別属性情報_BK更新処理 */
    /* 2023/05/29 MCCMPH2 ADD START */
    public int  UpdateOtodokesaki_bk();      /* MMお届け先情報 _BK更新処理 */
    /* 2023/05/29 MCCMPH2 ADD END */
    /* 2022/11/25 MCCM初版 DEL START */
    //static int  UpdateMatababy_bk();         /* MMマタニティベビー情報_BK更新処理 */
    /* 2022/11/25 MCCM初版 DEL END */
    public int  UpdateKokyakusedo_bk();      /* MS顧客制度情報_BK更新処理 */
    public int  UpdateMscard_bk();           /* MSカード情報_BK更新処理 */
    public int  UpdateRiyoukapoint_bk();     /* TS利用可能ポイント情報 */
    public int  UpdatePointyear_bk();        /* TSポイント年別情報_BK更新処理 */
    /* 2022/11/25 MCCM初版 DEL START */
    //static int  UpdateCirclekanri_bk();      /* MSサークル管理情報_BK更新処理 */
    //static int  DeleteCirclekanri_bk();      /* MSサークル管理情報_BK削除処理 */
    //static int  UpdateCirclekokyaku_bk();    /* MSサークル顧客情報_BK更新処理 */
    //static int  DeleteCirclekokyaku_bk();    /* MSサークル顧客情報_BK削除処理 */
    /* 2022/11/25 MCCM初版 DEL END */
    public int  UpdateKazokusedo_bk();       /* MS家族制度情報_BK更新処理 */
    public int  DeleteKazokusedo_bk();       /* MS家族制度情報_BK削除処理 */
    /* 2022/11/25 MCCM初版 ADD START */
    public int  UpdateGaibuninsho_bk();      /* MS外部認証情報_BK更新処理 */
    public int  DeleteGaibuninsho_bk();      /* MS外部認証情報_BK削除処理 */
    public int  UpdateTsrank_bk();           /* TSランク情報_BK更新処理 */
    public int  DeleteTsrank_bk();           /* TSランク情報_BK削除処理 */
    /* 2022/11/25 MCCM初版 ADD END */

}
