#!/bin/ksh
cd $CM_APWORK_DATE

RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_APWORK_DATE}/${CM_MYPRGID}4.log  2>&1 <<EOF
             \pset pager off
             \set AUTOCOMMIT off
             \set ON_ERROR_STOP true
             \set VERBOSITY verbose
             \i ${UPD_SQL_FILE}
             \o
    commit;
    \q
EOF
`
RTN=${?}

echo ${RTN}

