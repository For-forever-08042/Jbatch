#!/bin/ksh
###########################################
#  SYNONYM削除SQL実行
###########################################
echo "\pset pager off"                          >${TEMP_FILE8}.sql
echo "\set AUTOCOMMIT off"                     >>${TEMP_FILE8}.sql
echo "\set ON_ERROR_STOP true"                 >>${TEMP_FILE8}.sql
echo "\set VERBOSITY verbose"                  >>${TEMP_FILE8}.sql
echo "drop view  ${ORG_USR}.${SYN_NAME};"      >>${TEMP_FILE8}.sql
echo "commit;"                                 >>${TEMP_FILE8}.sql
echo "\q"                                      >>${TEMP_FILE8}.sql
RTN_VAL=`psql -q postgresql://${CONNECT_DB} -f ${TEMP_FILE8}.sql`
echo ${?}
echo ${RTN_VAL}
