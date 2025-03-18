cd $CM_APWORK_DATE
################################################
#  処理対象情報をジョブ実績ログファイルに出力
################################################
RTN_VAL=`psql -q postgresql://${CONNECT_MD} > ${CM_MYPRGID}.log 2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \t
         \pset footer off
         \set ECHO none
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \o ${CM_MYPRGID}.tmp
         SELECT COUNT(*)
         FROM    MMマタニティベビー情報
         WHERE  有効期限 < ${BAT_YYYYMMDD}
         AND      削除フラグ = 0;
         \o
         \q
EOF
`
echo ${?}