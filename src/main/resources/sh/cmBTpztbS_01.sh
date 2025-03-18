#!/bin/ksh
BAT_YYYYMMDD_1=`psql -q postgresql://${CONNECT_SD}  <<EOF
    \t
    \pset pager off
    \set ECHO none
    SELECT TO_CHAR(ADD_MONTHS(TO_DATE(${BAT_YYYYMMDD}),-1), 'YYYYMMDD') FROM DUAL;
    \q
EOF
`
echo ${BAT_YYYYMMDD_1}