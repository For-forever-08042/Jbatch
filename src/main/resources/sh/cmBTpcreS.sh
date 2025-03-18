#!/bin/ksh
cd ${CM_APWORK_DATE}
#################################################
#  リネーム後対象テーブル名存在チェックSQL文編集
#################################################
SQL_VALUE="select  'tbl_chk  ' || TABLENAME "
SQL_VALUE="${SQL_VALUE}  from PG_TABLES "
SQL_VALUE="${SQL_VALUE}  where TABLENAME =  '"
if test "${BKUP_FLG}" = 1
then
        SQL_VALUE="${SQL_VALUE}${NEW_BKUP_TBL}'"
else
        SQL_VALUE="${SQL_VALUE}${BASE_TBL_NAME}${CHG_NAME}'"
fi

###############################################
#  リネーム後対象テーブル名存在チェックSQL実行
###############################################
echo "\pset pager off"                     >${TEMP_FILE3}.sql
echo "\t"                                 >>${TEMP_FILE3}.sql
echo "\set AUTOCOMMIT off"                >>${TEMP_FILE3}.sql
echo "\set ON_ERROR_STOP true"            >>${TEMP_FILE3}.sql
echo "\set VERBOSITY verbose"             >>${TEMP_FILE3}.sql
echo "${SQL_VALUE};"                      >>${TEMP_FILE3}.sql
echo "commit;"                            >>${TEMP_FILE3}.sql
echo "\q"                                 >>${TEMP_FILE3}.sql
psql -q postgresql://${DB_CONN_VALUE} -f ${TEMP_FILE3}.sql >  ${TEMP_FILE4}.log  2>&1
echo ${?}
