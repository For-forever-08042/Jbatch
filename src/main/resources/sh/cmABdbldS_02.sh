#!/bin/ksh
###########################################
#  sqlldr実行
###########################################
export NLS_LANG=Japanese_Japan.JA16SJIS
cd ${CM_APWORK_DATE}
psql postgresql://${CONNECT_DB} -f ${SJIS_CTL}  >> ${LOG_FILE} 2>&1
echo ${?}
