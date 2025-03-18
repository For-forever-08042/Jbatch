#!/bin/ksh
###########################################
#  SQL実行
###########################################
echo "\pset pager off"                      >${TEMP_FILE1}.sql
echo "\set ECHO none"                      >>${TEMP_FILE1}.sql
echo "\t"                                  >>${TEMP_FILE1}.sql
echo "\set AUTOCOMMIT off"                 >>${TEMP_FILE1}.sql
echo "\set ON_ERROR_STOP true"             >>${TEMP_FILE1}.sql
echo "\set VERBOSITY verbose"              >>${TEMP_FILE1}.sql
echo "${SQL_VALUE};"                       >>${TEMP_FILE1}.sql
echo "commit;"                             >>${TEMP_FILE1}.sql
echo "\q"                                  >>${TEMP_FILE1}.sql
psql -q postgresql://${DB_CONN_VALUE} -f ${TEMP_FILE1}.sql >${TEMP_FILE2} 2>&1
echo ${?}