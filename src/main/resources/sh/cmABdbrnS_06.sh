#!/bin/ksh
###########################################
#  SQL実行
###########################################
echo "\pset pager off"                    >${TEMP_FILE6}.sql
echo "\set AUTOCOMMIT off"                >>${TEMP_FILE6}.sql
echo "\set ON_ERROR_STOP true"            >>${TEMP_FILE6}.sql
echo "\set VERBOSITY verbose"             >>${TEMP_FILE6}.sql
echo "alter index ${IDX_NAME} rename to ${IDX_NAME_NEW}"   >>${TEMP_FILE6}.sql
echo "commit;"                            >>${TEMP_FILE6}.sql
echo "\q"                                 >>${TEMP_FILE6}.sql
RTN_VAL=`psql -q postgresql://${CONNECT_DB} -f ${TEMP_FILE6}.sql`
RTN=${?}
echo ${RTN}
echo ${RTN_VAL}
