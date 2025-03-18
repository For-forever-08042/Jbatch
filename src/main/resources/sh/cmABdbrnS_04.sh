#!/bin/ksh
###########################################
#  インデックス名取得SQL文編集
###########################################
SQL_VALUE="select 'indx_select' as col1,indexname  from pg_indexes "
SQL_VALUE="${SQL_VALUE}  where tablename = '"
SQL_VALUE="${SQL_VALUE}${OPTION2}'"

###########################################
#  インデックス名取得SQL実行
###########################################
echo "\pset pager off"                     >${TEMP_FILE3}.sql
echo "\set echo none"                     >>${TEMP_FILE3}.sql
echo "\t"                                 >>${TEMP_FILE3}.sql
echo "\set AUTOCOMMIT off"                >>${TEMP_FILE3}.sql
echo "\set ON_ERROR_STOP true"            >>${TEMP_FILE3}.sql
echo "\set VERBOSITY verbose"             >>${TEMP_FILE3}.sql
echo "${SQL_VALUE}"                       >>${TEMP_FILE3}.sql
echo "commit;"                            >>${TEMP_FILE3}.sql
echo "\q"                                 >>${TEMP_FILE3}.sql
psql -q postgresql://${CONNECT_DB} -f ${TEMP_FILE3}.sql >${TEMP_FILE4} 2>&1
RTN=${?}
echo ${RTN}
