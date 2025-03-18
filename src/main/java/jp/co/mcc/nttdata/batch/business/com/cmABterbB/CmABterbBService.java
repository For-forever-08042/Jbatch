package jp.co.mcc.nttdata.batch.business.com.cmABterbB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.FileStatusDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto.TERA_OUT_FILE;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto.TERA_OUT_REC;
import jp.co.mcc.nttdata.batch.business.com.cmABterbB.dto.TERA_PARA;

import java.util.ArrayList;

public interface CmABterbBService {
    /*-----------------------------------------------------------------------------*/
    /*	関数プロトタイプ宣言                                                   */
    /*-----------------------------------------------------------------------------*/
    public int APLOG_WT(String msgid, int  msgidsbt, String dbkbn,
                        Object param1, Object param2, Object param3,
                        Object param4, Object param5, Object param6);
    public int parseArg(int argc, String[] argv, TERA_PARA para); /* 引数解析処理 */
    public int getParamFileName(String file_id, StringDto file_dir, StringDto file_name); /* パラメータファイル名取得 */
    public int readParamFile(String filename, String filedir, TERA_PARA para, StringDto sqlstr, TERA_OUT_FILE out_file, TERA_OUT_REC out_rec);
    public int mainProc(FileStatusDto fo, TERA_OUT_FILE out_file, TERA_OUT_REC out_rec, StringDto sqlstr, TERA_PARA t_para);
    public int APLOG_WT_902(int param1, String param2);
    public int APLOG_WT_903(String param1, int param2, int param3);

    public int splitRec(String in_str, IntegerDto out_cnt, ArrayList<String> out_str);
    public int editUtype(String ibuf, int siz, StringDto obuf);
    public int editXtype(StringDto ibuf, int siz, StringDto obuf, int sjis_flg, int dec_flg);
          /* 2022/11/09 MCCM初版 ADD START */
    public int editEDtype(StringDto ibuf, String dattyp, int siz, StringDto obuf, int encdec_flg);
    /* 2022/11/09 MCCM初版 ADD END */
}
