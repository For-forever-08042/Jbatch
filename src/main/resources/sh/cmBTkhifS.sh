#!/bin/ksh
cd ${CM_APWORK_DATE}
RTN_VAL1=`psql -q postgresql://${CONNECT_SD} >  ${CM_MYPRGID}.log  2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${SYS_YYYYMMDD}
         \i ${CM_APSQL}/${CM_MYPRGID}.sql
commit;
\q
EOF
`
echo ${?}