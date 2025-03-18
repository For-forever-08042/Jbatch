#!/bin/ksh
cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${SQLFile}.log  2>&1 <<EOF

         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${LAST_MONTH_YYYYMM} 
         \set 2 ${PZLP_O}_${SYS_YYYYMMDDHHMMSS}_1
         \i ${CM_APSQL}/${SQLFile}.sql
commit;
\q
EOF
`
echo ${?}
