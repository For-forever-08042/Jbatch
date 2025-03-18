#!/bin/ksh
cd ${CM_APWORK_DATE}
echo "\pset pager off"                     >${TEMP_FILE3}.sql
echo "\set ECHO none"                     >>${TEMP_FILE3}.sql
echo "\t"                                 >>${TEMP_FILE3}.sql
echo "\set AUTOCOMMIT off"                >>${TEMP_FILE3}.sql
echo "\set ON_ERROR_STOP true"            >>${TEMP_FILE3}.sql
echo "\set VERBOSITY verbose"             >>${TEMP_FILE3}.sql
echo "${SQL_VALUE};"                      >>${TEMP_FILE3}.sql
echo "commit;"                            >>${TEMP_FILE3}.sql
echo "\q"                                 >>${TEMP_FILE3}.sql
psql -q postgresql://${DB_CONN_VALUE} -f ${TEMP_FILE3}.sql >  ${TEMP_FILE4}.log  2>&1
echo ${?}
