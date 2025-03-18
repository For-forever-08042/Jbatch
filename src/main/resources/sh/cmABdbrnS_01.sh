#!/bin/ksh
###########################################
#  SYNONYM名取得SQL実行
###########################################
CONNECT_USR=`echo ${CONNECT_USR} | awk '{ print toupper($1) }'`
Select_synonym_value="select viewname,viewname from pg_views where schemaname='"
Select_synonym_value="${Select_synonym_value}${ORG_USR}' and viewowner='"
Select_synonym_value="${Select_synonym_value}${CONNECT_USR}' and viewname='"
Select_synonym_value="${Select_synonym_value}${OPTION1}'"
echo "\pset pager off"                     >${TEMP_FILE1}.sql
echo "\set AUTOCOMMIT off"                >>${TEMP_FILE1}.sql
echo "\set ECHO none"                     >>${TEMP_FILE1}.sql
echo "\set ON_ERROR_STOP true"            >>${TEMP_FILE1}.sql
echo "\set VERBOSITY verbose"             >>${TEMP_FILE1}.sql
echo "${Select_synonym_value}"            >>${TEMP_FILE1}.sql
echo "commit;"                            >>${TEMP_FILE1}.sql
echo "\q"                                 >>${TEMP_FILE1}.sql
psql -q postgresql://${CONNECT_DB} -f ${TEMP_FILE1}.sql >${TEMP_FILE7} 2>&1
RTN=${?}
echo ${RTN}
