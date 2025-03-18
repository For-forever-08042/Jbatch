/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw.com.exception;


import jp.co.mcc.nttdata.batch.fw.com.constants.CodeConstant;

/**
 * ベースクラス
 *
 * @author NCIT
 */
public interface AppException {
    /**
     * get http model code
     *
     * @return int
     */
    int getStatusCode();

    /**
     * get errorCode
     */
    String getErrorCode();

    /**
     * get error message
     */
    String getErrMessage();

    /**
     * get CodeConstant
     */
    CodeConstant getCodeConstant();
}
