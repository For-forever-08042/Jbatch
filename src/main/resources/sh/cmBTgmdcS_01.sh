#!/bin/ksh
cd $CM_APWORK_DATE

WSBATCH_EXIST_FLG=`psql -q postgresql://${CONNECT_SD}  <<EOF
            \t
            \pset pager off
            \set ECHO none
            select COUNT(1) from WSバッチ処理実行管理 where 機能ＩＤ = 'GMDC';
EOF
`
RTN=${?}
echo ${RTN} ${WSBATCH_EXIST_FLG}
