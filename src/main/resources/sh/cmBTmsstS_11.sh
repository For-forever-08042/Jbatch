#!/bin/ksh
cd ${CM_APWORK_DATE}

RTN_VAL=`psql -q postgresql://${CONNECT_SD} >  \${CM_MYPRGID}.log  2>&1 <<EOF
         \pset pager off
         \pset pager off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \i ${CM_MYPRGID}_6.sql
commit;
\q
EOF
`
echo ${?}
