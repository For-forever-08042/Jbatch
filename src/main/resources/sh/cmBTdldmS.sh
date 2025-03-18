#!/bin/ksh
cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log 2>&1 <<EOF
    \pset pager off
    \set AUTOCOMMIT off
    \pset footer off
    \t
    \set ECHO none
    \set ON_ERROR_STOP true
    \set VERBOSITY verbose
    \o ${CM_MYPRGID}.tmp
     SELECT DISTINCT CONCAT(T1.キャンペーンＩＤ, ',' , NULLIF(TRIM(T1.キャンペーン名称),''))
     FROM    MSキャンペーン情報 T1,
             MSキャンペーン管理情報 T2
     WHERE   T1.キャンペーン種別 = 1
     AND     T1.連動済みフラグ = 0
     AND     T2.最終更新日 >= ${BAT_YYYYMMDD_1}
     AND     T1.キャンペーンＩＤ = T2.キャンペーンＩＤ;
    \o
    \q
EOF
`
echo ${?}