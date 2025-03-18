#!/bin/ksh
cd ${CM_APWORK_DATE}

mv KK00000001.lst KK00000001_1

#  改行コードをCRLFに変換
cat KK00000001_1 | sed 's/$/\r/g' > KK00000001
rm -f KK00000001_1

################################################
#  非売上連動・年度ポイント還元実績作成情報処理
################################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}2.log 2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${BAT_YYYYMMDD}
         \i ${CM_APSQL}/${CM_MYPRGID}2.sql
commit;
\q
EOF
`
echo  ${?}
