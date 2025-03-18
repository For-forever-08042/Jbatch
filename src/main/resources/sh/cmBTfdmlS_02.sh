#!/bin/ksh
cd ${CM_APWORK_DATE}
RTN_VAL=`psql -q postgresql://${CONNECT_MD} > ${CM_MYPRGID}.log2 2>&1 <<EOF
             \pset pager off
             \set AUTOCOMMIT off
             \set ON_ERROR_STOP true
             \set VERBOSITY verbose
             UPDATE PMファイルダウンロード情報
             SET    処理ステータス = ${SYORI_STS},
                    バッチ更新日 = ${BAT_YYYYMMDD},
                    最終更新日 = ${BAT_YYYYMMDD},
                    最終更新日時 = SYSDATE(),
                    最終更新プログラムＩＤ = '${CM_MYPRGID}'
             WHERE  企業コード = ${READ_FLD1}
             AND    ＩＤ = ${READ_FLD2}
             AND    to_char(ダウンロード日, '00000000')=${READ_FLD3}
             AND    to_char(ダウンロード時刻, '000000')=${READ_FLD4};
    commit;
    \q
    EOF
`
echo ${?}
