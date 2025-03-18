#!/bin/ksh
RTN_VAL=`psql -q postgresql://${CONNECT_SD} 2>&1 <<EOF
    \t
    \pset pager off
    \set ECHO none
    select シーケンス番号 from WSバッチ処理実行管理 where 機能ＩＤ='FRNK';
 \q
eof
`

RTN=${?}
echo ${RTN} ${RTN_VAL}
