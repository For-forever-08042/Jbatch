#!/bin/ksh
###########################################
#  copy実行02
###########################################
export NLS_LANG=Japanese_Japan.JA16SJIS
cd ${WK_PWD}
psql postgresql://${CONNECT_DB} -f ${SJIS_CTL}  >> ${LOG_FILE} 2>&1
echo ${?}
