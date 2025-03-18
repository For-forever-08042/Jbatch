#!/bin/ksh
cd ${CM_APWORK_DATE}

###########################################
#  連動ファイル作成（商品DNA向け)
###########################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log  2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${BAT_YYYYMMDD} 
         \set 2 ${JOURNAL_RENKEI}.csv
         \i ${CM_APSQL}/cmBTjnnpS.sql
\q
EOF
`
echo  ${?}
