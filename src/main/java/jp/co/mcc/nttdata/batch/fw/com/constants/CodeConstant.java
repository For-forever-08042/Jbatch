/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw.com.constants;

/**
 * 共通コードクラス
 *
 * @author NCIT
 */
public enum CodeConstant {

    /**
     * Path Not Supported
     */
    E000006("sys.message.e000002");

    public final String messageCode;

    CodeConstant(String messageCode) {
        this.messageCode = messageCode;
    }


}
