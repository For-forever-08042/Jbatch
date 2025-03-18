#!/bin/ksh
################################################
#  処理対象情報をジョブ実績ログファイルに出力
################################################
cd ${CM_APWORK_DATE}
echo "\pset pager off"                    >${TEMP_FILE3}.sql
echo "\set AUTOCOMMIT off"                >>${TEMP_FILE3}.sql
echo "\set ON_ERROR_STOP true"            >>${TEMP_FILE3}.sql
echo "\set VERBOSITY verbose"             >>${TEMP_FILE3}.sql
echo "delete from ${TBL_NAME} where ${SQL_WHERE};" >>${TEMP_FILE3}.sql
echo "commit;"                            >>${TEMP_FILE3}.sql
echo "\q"                                 >>${TEMP_FILE3}.sql
psql -q postgresql://${Conn_DB} -f ${TEMP_FILE3}.sql >${TEMP_FILE4} 2>&1
echo ${?}