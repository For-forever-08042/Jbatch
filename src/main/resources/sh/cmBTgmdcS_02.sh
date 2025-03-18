#!/bin/ksh
cd $CM_APWORK_DATE

RTN_VAL=`psql -q postgresql://${CONNECT_SD}  <<EOF
                \t
                \pset pager off
                \set ECHO none
                select シーケンス番号 from WSバッチ処理実行管理 where 機能ＩＤ = 'GMDC';
EOF
`

RTN=${?}
echo ${RTN} ${RTN_VAL}

