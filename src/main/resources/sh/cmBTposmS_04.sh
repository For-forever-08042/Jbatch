#!/bin/ksh
cd ${CM_APWORK_DATE}

###########################################
#  連動ファイル作成（RetailCRM向け)
###########################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log  2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${BAT_YYYYMMDD} 
         \set 2 ${URIAGE_Retail}.csv
         \i ${CM_APSQL}/cmBTpos1S.sql
\q
EOF
`
echo  ${?}
