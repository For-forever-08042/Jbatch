#!/bin/ksh
cd ${CM_APWORK_DATE}

mv KK00000003.lst KK00000003_1

#  改行コードをCRLFに変換
cat KK00000003_1 | sed 's/$/\r/g' > KK00000003
rm -f KK00000003_1

####################################################
#  非売上連動・期間限定ポイント還元実績作成情報処理
####################################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}4.log 2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${BAT_YYYYMMDD}
         \i ${CM_APSQL}/${CM_MYPRGID}4.sql
commit;
\q
EOF
`
echo  ${?}
