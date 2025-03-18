#!/bin/ksh
cd ${CM_APWORK_DATE}

###########################################
# WS売上明細会員ＧＯＯＰＯＮ番号登録
###########################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD}  2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \i ${CM_APSQL}/${CM_MYPRGID}.sql
commit;
\q
EOF
`
echo  ${?}
