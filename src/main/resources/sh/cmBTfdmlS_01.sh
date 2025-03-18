cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_MD} > ${CM_MYPRGID}.log 2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \t
         \pset footer off
         \set ECHO none
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \o ${TEMP_FILE1}.lst
         SELECT concat( nullif(trim(cast(企業コード as varchar)),''), ',' ,
                 nullif(trim(cast(ｉｄ as varchar)),'') , ',' ,
                 nullif(trim(to_char(ダウンロード日, '00000000')),'') , ',' ,
                 nullif(trim(to_char(ダウンロード時刻, '000000')),'') , ',' ,
                 nullif(trim(氏名),'') , ',' ,
                 nullif(trim(ファイル名),''))
         FROM   PMファイルダウンロード情報
         WHERE  処理ステータス = ${SYORI_STS_MI}
         ORDER BY ダウンロード日, ダウンロード時刻;
\o
\q
EOF
`
echo ${?}
