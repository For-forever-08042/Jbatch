package jp.co.mcc.nttdata.batch.business.com.cmABendeB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;

public interface CmABendeBService {

    int C_EncDec_Pid(int endeFlg, String pattern,
                     String indata, StringDto outdata);

    int C_EncDec_DES(int endeFlg, String key, int keyLen,
                     String indata, int indataLen,
            /* 2022/10/25 MCCM初版 MOD START */
//unsigned char *outdata, int *outdataLen)*/
                     StringDto outdata, IntegerDto outdataLen, int padding);

    int C_EncDec_AES(int endeFlg, String key, int keyLen,
                     String iv, int ivLen, String indata, int indataLen,
                     StringDto outdata, IntegerDto outdataLen);
}
