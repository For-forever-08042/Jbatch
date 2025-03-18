package jp.co.mcc.nttdata.batch.business.service.cmBTschuB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTschuBService {

    public int  KokyakuLock();                        /* 顧客ロック処理           */
    public int  UpdateShussanHakkouKahi();            /* 顧客制度情報更新処理     */
    public int  cmBTschuB_Chk_Arg(StringDto Arg_in);
    public int  cmBTschuB_Chk_HakkoKahi(int age, int days);

}
