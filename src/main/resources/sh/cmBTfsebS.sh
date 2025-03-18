#!/bin/ksh
###########################################
# 不正防止データ（1万P以上）付与を出力
###########################################
cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log 2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${SYS_YYYYMMDD}
         \set 2 ${LAST_MONTH}
         \i ${CM_APSQL}/${CM_MYPRGID}.sql
commit;
\q
EOF
`
echo ${?}
