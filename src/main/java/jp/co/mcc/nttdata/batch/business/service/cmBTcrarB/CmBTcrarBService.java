package jp.co.mcc.nttdata.batch.business.service.cmBTcrarB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmBTcrarBService {
     int cmBTcrarB_main();
     int Make_List_File();
     int Write_List_File();
     int Chk_ArgoInf(StringDto Arg_in, int arg_cnt);
}
