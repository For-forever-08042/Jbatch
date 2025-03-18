#!/bin/ksh
psql postgresql://${CONNECT_MD} $(cat ${TEMP_FILE1}) > ${TEMP_FILE3} 2>&1
echo ${?}
