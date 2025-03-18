#!/bin/ksh
cd ${CM_APWORK_DATE}
###########################################
#  WSＳＡＰ付与還元実績抽出（当月分）
###########################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}10.log 2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${BAT_YYYYMM} 
         \set 2 ${BAT_YYYYMMDD}
         \i ${CM_APSQL}/${CM_MYPRGID}10.sql
commit;
\q
EOF
`
echo  ${?}
