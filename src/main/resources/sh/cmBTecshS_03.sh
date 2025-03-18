#!/bin/ksh
cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log 2>&1 <<EOF
    \pset pager off
    \set AUTOCOMMIT off
    \set ON_ERROR_STOP true
    \set VERBOSITY verbose
    \set 1 ${BAT_YYYYMMDD}
    \set 2 ${SPSS_RENDO_FILE}.csv_1
    \i ${CM_APSQL}/cmBTecsh2S.sql
            \q
EOF
`
echo ${?}