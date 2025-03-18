#!/bin/ksh
cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log 2>&1 <<EOF
    \pset pager off
    \set AUTOCOMMIT off
    \set ON_ERROR_STOP true
    \set VERBOSITY verbose
    \set 1 ${BAT_YYYYMMDD} 
    \set 2 ${DNA_RENDO_FILE}.tmp
    \i ${CM_APSQL}/cmBTjnecS.sql
    \q
EOF
`
echo ${?}