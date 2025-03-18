###########################################
#  パラメータファイルから抽出したデータ読み込む
###########################################
cat ${IN_FILE} | while read  READ_FLD1 READ_FLD2 READ_FLD3 READ_FLD4 READ_FLD5 READ_FLD6
do
        if test "${READ_FLD1}" = "#"
        then
                continue
        fi
        HOST_NAME=${READ_FLD1}
        TBL_NAME=${READ_FLD2}
        TBL_FILE_ORG=${READ_FLD3}
        USR_NAME=`eval echo ${READ_FLD4}`
        PSW_NAME=`eval echo ${READ_FLD5}`
        SID_NAME=`eval echo ${READ_FLD6}`
        CHG_NAME=""

        # ファイル名とファイル番号取得
        TBL_FILE_LEN=`echo ${#TBL_FILE_ORG}`
        TBL_FILE_LEN_F=`expr ${TBL_FILE_LEN} - 3`
        TBL_FILE_LEN_D=`expr ${TBL_FILE_LEN} - 1`
        TBL_FILE_NO=`echo ${TBL_FILE_ORG} | cut -c${TBL_FILE_LEN_D}-`
        TBL_FILE=`echo ${TBL_FILE_ORG} | cut -c1-${TBL_FILE_LEN_F}`

        # 末尾にYYYYがあるかチェック
        TBL_TAIL_YB=`expr "${TBL_NAME}" : ".*\(YYYYB\)"`

        # 末尾にYYYYがあるかチェック
        TBL_TAIL_YT=`expr "${TBL_NAME}" : ".*\(YYYYT\)"`

        ###########################################
        #  リネーム後可変部の編集
        ###########################################
        if test "${TBL_TAIL_YB}" = "YYYYB"
        then
                CHG_NAME=${BAT_YYYY_T}
                BASE_TBL_NAME=`echo ${TBL_NAME} | sed 's/YYYYB//'`
                BASE_TBL_NAME=`echo ${BASE_TBL_NAME}${BAT_YYYY_B}`
                BASE_TBL_FILE=`echo ${TBL_FILE} | sed 's/YYYYB//'`
                BASE_TBL_FILE=`echo ${BASE_TBL_FILE}${BAT_YYYY_B}`
        elif test "${TBL_TAIL_YT}" = "YYYYT"
        then
                CHG_NAME=${BAT_YYYY_T}
                BASE_TBL_NAME=`echo ${TBL_NAME} | sed 's/YYYYT//'`
                BASE_TBL_NAME=`echo ${BASE_TBL_NAME}${BAT_YYYY_T}`
                BASE_TBL_FILE=`echo ${TBL_FILE} | sed 's/YYYYT//'`
                BASE_TBL_FILE=`echo ${BASE_TBL_FILE}${BAT_YYYY_T}`
        fi

        ###########################################
        #  バックアップ開始ファイル作成
        ###########################################
        if test "${CHG_NAME}" = ""
        then
                echo "${HOST_NAME} ${TBL_NAME} ${TBL_FILE}_${TBL_FILE_NO} ${USR_NAME} ${PSW_NAME} ${SID_NAME} ${PROC_YEDAY}" >>${WORK_DIR}/${DB_SERVER_NAME}${BKUP_START_FNAME}
        else
                echo "${HOST_NAME} ${BASE_TBL_NAME} ${BASE_TBL_FILE}_${TBL_FILE_NO} ${USR_NAME} ${PSW_NAME} ${SID_NAME} ${PROC_YEDAY}" >>${WORK_DIR}/${DB_SERVER_NAME}${BKUP_START_FNAME}
        fi

done

###########################################
#  バックアップ開始ファイル送信
#  バックアップ開始OKファイル作成
###########################################
cp -p ${WORK_DIR}/${DB_SERVER_NAME}${BKUP_START_FNAME} ${SEND_DIR}/${DB_SERVER_NAME}${BKUP_START_FNAME} > /dev/null 2>&1
touch ${SEND_DIR}/${DB_SERVER_NAME}${START_OK_FNAME} 2>/dev/null
chmod 666 ${SEND_DIR}/${DB_SERVER_NAME}${BKUP_START_FNAME}
chmod 666 ${SEND_DIR}/${DB_SERVER_NAME}${START_OK_FNAME}
echo ${?}
