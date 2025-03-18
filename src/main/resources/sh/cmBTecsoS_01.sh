#!/bin/ksh
cd ${CM_APWORK_DATE}
###########################################
# WS会員GOOPON番号登録
###########################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log 2>&1 <<EOF
                  \pset pager off
                  \set AUTOCOMMIT off
                  \set ON_ERROR_STOP true
                  \set VERBOSITY verbose
                  \i ${CM_APSQL}/cmBTecshS.sql
commit;
\q
EOF
`
echo  ${?}
