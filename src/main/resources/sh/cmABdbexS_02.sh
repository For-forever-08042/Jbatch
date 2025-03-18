#!/bin/ksh
###########################################
#  pg_dump実行
###########################################
pg_dump postgresql://${CONNECT_MD} $(cat ${TEMP_FILE2}) -a > ${TEMP_FILE3} 2>&1
echo ${?}
