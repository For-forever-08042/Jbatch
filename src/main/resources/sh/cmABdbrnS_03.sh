#!/bin/ksh
###########################################
#  テーブルリネームSQL実行
###########################################
echo "\pset pager off"                  >${TEMP_FILE2}.sql
echo "\set AUTOCOMMIT off"              >>${TEMP_FILE2}.sql
echo "\set ON_ERROR_STOP true"          >>${TEMP_FILE2}.sql
echo "\set VERBOSITY verbose"           >>${TEMP_FILE2}.sql
echo "ALTER TABLE ${OPTION1} RENAME TO  ${OPTION2};"    >>${TEMP_FILE2}.sql
echo "commit;"                          >>${TEMP_FILE2}.sql
echo "\q"                               >>${TEMP_FILE2}.sql
RTN_VAL=`psql -q postgresql://${CONNECT_DB} -f ${TEMP_FILE2}.sql`

RTN=${?}
echo ${RTN}
echo ${RTN_VAL}
