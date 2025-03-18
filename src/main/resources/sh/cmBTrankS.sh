#!/bin/ksh
RTN_VAL=`psql -q postgresql://${CONNECT_SD}  <<eof
    \t
    \pset pager off
    \set ECHO none
    select シーケンス番号 from WSバッチ処理実行管理 where 機能ＩＤ='RANK';
  \q
eof
`
echo ${?} ${RTN_VAL}
