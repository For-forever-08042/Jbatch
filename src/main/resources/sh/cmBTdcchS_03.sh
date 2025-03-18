#!/bin/ksh
DCCNT=`cat ${TEMP_FILE2} | sed  -e "/^$/d" | sed  -e "s/ //g"`
echo ${DCCNT}