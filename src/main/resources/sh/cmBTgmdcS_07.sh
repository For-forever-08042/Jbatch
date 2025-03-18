#!/bin/ksh
cd $CM_APWORK_DATE

RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_APWORK_DATE}/${CM_MYPRGID}5.log  2>&1 <<EOF
             \pset pager off
             \set AUTOCOMMIT off
             \set ON_ERROR_STOP true
             \set VERBOSITY verbose
             \set 1 ${TEMP_FILE04}.csv
             \i ${CM_APSQL}/${CM_MYPRGID}3.sql
    commit;
    \q
EOF
`
RTN=${?}

echo ${RTN}

