#!/bin/ksh
SQL_CD=`cat ${TEMP_FILE2} | sed s/ORA-/'\n'ORA-/ | grep "ORA-" |  cut -c5-9`
echo ${SQL_CD}