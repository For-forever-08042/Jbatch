package jp.co.mcc.nttdata.batch.business.job.cmABmexpS;

import jp.co.mcc.nttdata.batch.business.service.db.ShellClientManager;
import jp.co.mcc.nttdata.batch.business.service.db.dto.ShellExecuteDto;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * #-------------------------------------------------------------------------------
 * #    名称　　　　　：　メール送信
 * #    プログラムID　：　cmABmexpS
 * ################################################################################
 * # ！！！要注意！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
 * # ！　本番環境ではGmailを利用してのメール送信のため、当該シェルは本番環境では ！
 * # ！　使用不可となります。                                                    ！
 * # ！　修正が必要な場合は、本番環境のシェルを直接編集してください。            ！
 * # ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
 * ################################################################################
 * #
 * #    【処理概要】
 * #       起動引数で渡された宛先に、メールを送信する。
 * #
 * #    【引数説明】
 * #       第１引数                :　SMTPサーバー
 * #       第２引数                :　クライアントドメイン
 * #       第３引数                :　メールの送り主アドレス
 * #       第４引数                :　メールの宛先アドレス
 * #       第５引数                :　メール件名
 * #       第６引数                :　メール本文の内容を格納しているテキストファイル
 * #       第７引数                :　メールの宛先アドレスCC
 * #
 * #    【戻り値】
 * #       10　　 ：　正常
 * #       49　　 ：　警告
 * #
 * #-------------------------------------------------------------------------------
 * #    稼働環境
 * #      Red Hat Enterprise Linux 6
 * #
 * #    改定履歴
 * #      1.00 :   2015/09/07 SSI.上野  ： 初版
 * #     40.00 :   2023/05/31 SSI.張シン  ： MCCM初版（CC追加）
 * #-------------------------------------------------------------------------------
 * #  $Id:$
 * #-------------------------------------------------------------------------------
 * #  Copyright (C) 2012 NTT DATA CORPORATION
 * #-------------------------------------------------------------------------------
 */
@Slf4j
@Component
public class CmABmexpSTask extends NtBasicTask {

  @Override
  public int taskExecuteCustom(String[] args) {

    System.out.println(ARG_ALL);
    System.out.println(" 第１引数  :　SMTPサーバー :　" + args[0]);
    System.out.println("  第２引数 :　クライアントドメイン :　" + args[1]);
    System.out.println("  第３引数 :　メールの送り主アドレス :　" + args[2]);
    System.out.println("  第４引数 :　メールの宛先アドレス :　" + args[3]);
    System.out.println("  第５引数 :　メール件名 :　" + args[4]);
    System.out.println("  第６引数 :　メール本文の内容を格納しているテキストファイル :　" + args[5]);
    System.out.println("  第７引数 :　メールの宛先アドレスCC :　" + args[6]);
    //        ShellExecuteDto shellExecuteDto = ShellClientManager.getShellExecuteDto(CM_MYPRGID, args).execute();
    return Rtn_OK;
  }
}
