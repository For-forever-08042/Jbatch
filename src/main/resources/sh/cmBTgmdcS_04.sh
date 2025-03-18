#!/bin/ksh
cd $CM_APWORK_DATE

COL_NUM=`head -n +1 ${TEMP_FILE01}.csv | awk -F ',' '{print NF}'`
if test ${COL_NUM} -eq 4
then
    #  改行コードからCRを削除  (SPSS/RetailCRM)
    cat ${CM_APWORK_DATE}/${TEMP_FILE01}.csv | tr -d '\r' > ${TEMP_CTL_FILE}.tmp 2> /dev/null
else
    # ファイル項目数が５の場合、登録日を抜く(商品DNA）
    cat ${CM_APWORK_DATE}/${TEMP_FILE01}.csv | tr -d '\r' | awk -F ',' '{print $1","$2","$3","$5}' > ${TEMP_CTL_FILE}.tmp 2> /dev/null
fi

