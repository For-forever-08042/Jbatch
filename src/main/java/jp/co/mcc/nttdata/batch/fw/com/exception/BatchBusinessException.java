/*
 * ====================================================================
 * Copyright © 2021 NTT DATA CORPORATION All rights reserved.
 * ====================================================================
 */

package jp.co.mcc.nttdata.batch.fw.com.exception;


import jp.co.mcc.nttdata.batch.fw.com.constants.CodeConstant;
import jp.co.mcc.nttdata.batch.fw.util.MessageUtil;

/**
 * バッチ異常クラス
 *
 * @author sys
 */
public class BatchBusinessException extends RuntimeException implements AppException {

    /**
     * exception error code
     */
    private final CodeConstant errCodeInfo;


    /**
     * exception message
     */
    private final String message;

    /**
     * exception message
     */
    private final Object data;

    /**
     * constructor
     */
    public BatchBusinessException() {
        this.message = null;
        this.errCodeInfo = null;
        this.data = null;
    }

    /**
     * constructor
     *
     * @param errCode {@link CodeConstant }
     * @param message String
     */
    public BatchBusinessException(CodeConstant errCode, String... message) {
        this.message = MessageUtil.getLocaleMessageSourceService().getMessage(errCode, message);
        this.errCodeInfo = errCode;
        this.data = null;
    }

    public Object getData() {
        return data;
    }

    /**
     * get statusCode
     *
     * @return statusCode
     */
    @Override
    public int getStatusCode() {
        return 0;
    }

    /**
     * set statusCode
     *
     * @return errCode
     */
    @Override
    public String getErrorCode() {
        return this.errCodeInfo.name();
    }

    /**
     * get message
     *
     * @return message
     */
    @Override
    public String getErrMessage() {

        return message;

    }

    /**
     * get CodeConstant
     *
     * @return CodeConstant
     */
    @Override
    public CodeConstant getCodeConstant() {
        return this.errCodeInfo;
    }
}
