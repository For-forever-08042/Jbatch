package jp.co.mcc.nttdata.batch.business.com.cmABaplwB;

public interface CmABaplwBService {

    public class AplwParam {
        String p_msgid;
        String p_msgty;
        String p_flg;
        String p_msg;
        String p_mod;
        String p_modname;
    }

    public int param_proc(int argc, String[] argv, AplwParam param);

}
