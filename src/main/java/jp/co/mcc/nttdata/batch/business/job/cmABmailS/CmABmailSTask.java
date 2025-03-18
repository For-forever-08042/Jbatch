package jp.co.mcc.nttdata.batch.business.job.cmABmailS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.business.job.cmABmexpS.CmABmexpSTask;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//#!/bin/ksh
//        #-------------------------------------------------------------------------------
//        #    名称　　　　　：　メール送信
//        #    プログラムID　：　cmABmailS
//        #
//        #    【処理概要】
//        #       起動引数で渡された宛先に、メールを送信する。
//        #
//        #    【引数説明】
//        #       第１引数                :　メールの宛先アドレス
//        #       第２引数                :　メールの送り主アドレス
//        #       第３引数                :　メール件名
//        #       第４引数                :　メール本文の内容を格納しているテキストファイル
//        #       第５引数                :　メールの送り主アドレスCC
//        #
//        #    【戻り値】
//        #       10　　 ：　正常
//        #       99　　 ：　異常
//        #       49　　 ：　警告（メール送信不可）
//        #-------------------------------------------------------------------------------
//        #    稼働環境
//        #      Red Hat Enterprise Linux 6
//        #
//        #    改定履歴
//        #      1.00 :   2011/10/18 SSI.吉岡  ： 初版
//        #      1.01 :   2011/12/25 SSI.本田  ： メール本文ファイルは環境変数（CM_MAILTEXT）
//        #                                       から取得
//        #      4.00 :   2014/08/20 SSI.上野  ： メール送信結果をログに出力
//        #      5.00 :   2015/09/08 SSI.上野  ： メール送信対話式変更
//        #      6.00 :   2021/10/05 SSI.上野  ： 接続アカウント変更
//        #     40.00 :   2023/05/25 SSI.張シン  ： MCCM初版（cc追加）
//        #-------------------------------------------------------------------------------
//        #  $Id:$
//        #-------------------------------------------------------------------------------
//        #  Copyright (C) 2012 NTT DATA CORPORATION
//        #-------------------------------------------------------------------------------
@Slf4j
@Component
public class CmABmailSTask extends NtBasicTask {
    @Autowired
    CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    CmABmexpSTask cmABmexpSTask;

    @Override
    public int taskExecuteCustom(String[] args) {
//###########################################
//#  プログラムIDを環境変数に設定
//###########################################
//        CM_MYPRGNAME = "メール送信";
//        setenv(CmABfuncLServiceImpl.CM_MYPRGID, CM_MYPRGID);

//###########################################
//#  開始メッセージをAPログに出力
//###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().S(CM_MYPRGNAME));

//###########################################
//#  定数定義
//###########################################
//#  戻り値
        int Rtn_OK = 10;
        int Rtn_NG = 99;
        int Rtn_WN = 49;
        int Rtn_EXIT = Rtn_OK;

//# SMTPサーバー
        String MAILSVR = "172.22.21.241";
//# クライアントドメイン
        String MAILDOMAIN = "TEST";
//# 接続アカウント
        String ACCOUNT = "cfos_wf@matsukiyococokara.com";
//# パスワード
        String PASSWORD = "k98yOUbg";

//# システム日時
        String SYS_YYYYMMDDHHMMSS = DateUtil.nowDateFormat("yyyyMMddhhmmss");

//#  その他
        String BKUP_IFS = IFS;

//#  待機時間
        int WAIT_TIME = 10;

//#  メール送信回数
        int SEND_CNT = 0;
        int SEND_MAXCNT = 3;

//###########################################
//#  APログ出力関数
//###########################################
//#警告終了メッセージを出力しないため
//#    if test "$2" != "-FI"

//###########################################
//#  引数チェック
//###########################################
//        set -- $*
        if (args.length < 4 || args.length > 5) {
            APLOG_WT("引数エラー  [" + ARG_ALL + "]", FW);
            return Rtn_NG;
        }


//#  引数格納
        String TO_ADDR = args[0];
        String FROM_ADDR = args[1];
        String MAIL_SUBJECT = args[2];
        String FILE_BODY = CM_MAILTEXT + "/" + args[3];
        String CC_ADDR = null;
        if (args.length == 5) {
            CC_ADDR = args[4];
        }

//###########################################
//#  本文ファイル存在チェック
//###########################################
        if (!FileUtil.isExistFile(FILE_BODY)) {
            APLOG_WT("本文格納ファイルなし ファイル名=${" + FILE_BODY + "}", FW);
            return Rtn_NG;
        }


//###########################################
//#  メール送信
//###########################################
//# メール件名（文字コード変換）
        String MAIL_SUBJECT_ICONV = "";
        MAIL_SUBJECT_ICONV = IconvUtil.main("UTF-8", "iso-2022-jp", MAIL_SUBJECT);
//        MAIL_SUBJECT_ICONV=`echo "${MAIL_SUBJECT}" | iconv -c -f UTF-8 -t `
//# メール本文（文字コード変換）
        String FILE_BODY_ICONV = args[3] + "_" + SYS_YYYYMMDDHHMMSS + PidUtil.getPid();

//#cat ${FILE_BODY} | iconv -c -f UTF-8 -t MS932 > ${FILE_BODY_ICONV}
        IconvUtil.main("UTF-8", "iso-2022-jp", FILE_BODY, CM_APWORK_DATE + "/" + FILE_BODY_ICONV);
//        cat ${FILE_BODY} | iconv -c -f UTF-8 -t iso-2022-jp > ${FILE_BODY_ICONV}


//# 接続アカウント（暗号化）
        String ACCOUNT_ENC = Base64Utils.encode(ACCOUNT.getBytes());
//# パスワード（暗号化）
        String PASSWORD_ENC = Base64Utils.encode(PASSWORD.getBytes());

        while (true) {
            SEND_CNT = SEND_CNT + 1;

            String[] argsArray = {MAILSVR, MAILDOMAIN, FROM_ADDR, TO_ADDR, MAIL_SUBJECT_ICONV, FILE_BODY_ICONV, ACCOUNT_ENC, PASSWORD_ENC, CC_ADDR};
            int RTN = cmABmexpSTask.main(getExecuteBaseParam().add(argsArray));
            FileUtil.writeFile(CM_APWORK_DATE + "/" +SYS_YYYYMMDDHHMMSS + "_mailsend." + PidUtil.getPid() + "_" + SEND_CNT, String.valueOf(RTN));
            if (Rtn_OK == RTN) {
                APLOG_WT("メール送信に成功しました（" + SEND_CNT + "回目）", FI);
//                            # 正常終了（処理継続）
                break;
            } else {
                if (SEND_CNT >= SEND_MAXCNT) {
//                                # メール送信失敗（サーバエラー）かつ送信回数に達した場合、警告終了
                    APLOG_WT("メール送信に失敗しました[" + RTN + "]（サーバエラー：" + SEND_CNT + "回失敗のため終了）", FW);
                    Rtn_EXIT = Rtn_WN;
                    break;
                }

//                        # メール送信失敗（サーバエラー）の場合、待機後再実行
                APLOG_WT("メール送信に失敗しました[" + RTN + "]（サーバエラー：" + SEND_CNT + "回目）", FI);
                try {
                    Thread.sleep(WAIT_TIME * 1000);
                } catch (Exception e1) {

                }
                continue;
            }
        }

//#(set codeset JIS Kanji
//# echo "ehlo ${MAILDOMAIN}"
//# sleep 1
//# echo "auth login"
//# sleep 1
//# echo "`echo ${ACCOUNT} | openssl enc -e -base64`"
//# sleep 1
//# echo "`echo ${PASSWORD} | openssl enc -e -base64`"
//# sleep 1
//# echo "mail from:<${FROM_ADDR}>"
//# sleep 1
//# echo "rcpt to:<${TO_ADDR}>"
//# sleep 3
//# echo "data"
//# sleep 2
//# echo "Content-Type: text/plain; charaset=\"iso-2022-jp\""
//# sleep 2
//# echo -n "Subject:"
//# echo "${MAIL_SUBJECT}" | iconv -c -f UTF-8 -t iso-2022-jp
//# sleep 2
//# echo "FROM:${FROM_ADDR}"
//# sleep 2
//# echo "TO:${TO_ADDR}"
//# sleep 2
//# echo ""
//# sleep 2
//# cat ${FILE_BODY} | iconv -c -f UTF-8 -t MS932
//# echo "."
//# sleep 2
//# echo "QUIT") | openssl s_client -starttls smtp -crlf -connect ${MAILSVR}:587 > ${SYS_YYYYMMDDHHMMSS}_mailsend.$$
//# #echo "QUIT") | telnet ${MAILSVR} 25 > /dev/null 2>&1
//
//###########################################
//#  終了メッセージをAPログに出力
//###########################################
        cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).FI());
        return Rtn_EXIT;
    }

    @Override
    public void APLOG_WT(String... args) {
        IFS = "@";
        setenv("CM_MYPRGID",CM_MYPRGID);
        cmABaplwBServiceImpl.main(getExecuteBaseParam().add(args));
        if (FE.equals(args[1])) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().E(CM_MYPRGNAME).add(args[1]));
        }
        IFS = getenv(CmABfuncLServiceImpl.BKUP_IFS);
    }
}
