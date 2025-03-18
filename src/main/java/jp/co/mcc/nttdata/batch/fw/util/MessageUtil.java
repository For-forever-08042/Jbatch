/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw.util;

/**
 * メッセージユーティリティソフト
 *
 * @author NCIT
 */
public class MessageUtil {

    private static LocaleMessageUtil localeMessageUtil;

    protected static void setLocaleMessageSourceService(LocaleMessageUtil localeMessageUtil) {
        MessageUtil.localeMessageUtil = localeMessageUtil;
    }

    public static LocaleMessageUtil getLocaleMessageSourceService() {
        return localeMessageUtil;
    }
}
