#!/bin/ksh
###########################################
#  SYNONYM作成SQL実行
###########################################
echo "\pset pager off"                         >${TEMP_FILE8}.sql
echo "\set AUTOCOMMIT off"                    >>${TEMP_FILE8}.sql
echo "\set ON_ERROR_STOP true"                >>${TEMP_FILE8}.sql
echo "\set VERBOSITY verbose"                 >>${TEMP_FILE8}.sql
echo "CREATE VIEW ${ORG_USR}.${OPTION2} AS SELECT * FROM ${CONNECT_USR}.${OPTION2};"   >>${TEMP_FILE8}.sql
echo "commit;"                                >>${TEMP_FILE8}.sql
echo "\q"                                     >>${TEMP_FILE8}.sql
RTN_VAL=`psql -q postgresql://${CONNECT_DB} -f ${TEMP_FILE8}.sql`

RTN=${?}
echo ${RTN}
echo ${RTN_VAL}
