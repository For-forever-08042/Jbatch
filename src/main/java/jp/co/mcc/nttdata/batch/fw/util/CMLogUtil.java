/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw.util;

import lombok.extern.slf4j.Slf4j;


/**
 * ログコードクラス
 *
 * @author NCIT
 */
@Slf4j
public class CMLogUtil {


    /**
     * info log to STDOUT
     *
     * @param msg メッセージ
     */
    public static void info(String msg) {
        System.out.println(msg);
    }

}
