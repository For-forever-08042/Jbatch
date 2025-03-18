#!/bin/ksh
cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_SD} >  ${CM_APWORK_DATE}/${CM_MYPRGID}.log  2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \i ${CM_APWORK_DATE}/${CM_MYPRGID}_2.sql
commit;
\q
EOF
`
echo ${?}
