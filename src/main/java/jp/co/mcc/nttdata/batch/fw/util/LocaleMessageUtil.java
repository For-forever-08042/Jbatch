/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw.util;

import jp.co.mcc.nttdata.batch.fw.com.constants.CodeConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * メッセージサービス
 *
 * @author NCIT
 */
@Component
public class LocaleMessageUtil {


    @Value("${tag.language}")
    private String language;


    @Resource
    MessageSource messageSource;

    /**
     * @param code key of message  {@link CodeConstant}
     * @return String
     */
    public String getMessage(CodeConstant code) {
        return getMessage(code, "");
    }


    /**
     * get message
     *
     * @param code code of message   {@link CodeConstant}
     * @param args String[]
     * @return String
     */
    public String getMessage(CodeConstant code, String... args) {
        Locale locale = Locale.forLanguageTag(language);
        return getMessage(code, args, locale);
    }

    /**
     * get message
     *
     * @param code   key of message  {@link CodeConstant}
     * @param args   String[]
     * @param locale {@link Locale}
     * @return String
     */
    public String getMessage(CodeConstant code, String[] args, Locale locale) {
        return messageSource.getMessage(code.messageCode, args, locale);
    }
}
