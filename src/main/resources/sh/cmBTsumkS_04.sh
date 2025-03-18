#!/bin/ksh
cd ${CM_APWORK_DATE}

##############################################
#  売上連動・年度ポイント付与実績作成情報処理
##############################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}1.log 2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${BAT_YYYYMMDD}
         \i ${CM_APSQL}/${CM_MYPRGID}1.sql
commit;
\q
EOF
`
echo  ${?}
