#!/bin/ksh
cd $CM_APWORK_DATE

# 重複チェック処理
# 親ＧＯＯＰＯＮ番号、子ＧＯＯＰＯＮ番号、削除フラグをキーにして、重複のデータを削除する　※１行のみの状態にする
cat ${TEMP_FILE05}_1.csv | awk -F, 'a[$1,$2,$3]++ == 0' >  ${TEMP_FILE05}_2.csv

# 親ＧＯＯＰＯＮ番号、子ＧＯＯＰＯＮ番号をキーにして、重複のデータをすべて削除する　※両方とも削除、0件の状態
cat ${TEMP_FILE05}_2.csv | awk -F, 'a[$1,$2]++ == 1{print $1","$2}' > ${TEMP_FILE05}_3.csv

cat ${TEMP_FILE05}_3.csv | while read LINE
do
    COL1=`echo ${LINE} | awk -F ',' '{print $1}'`
    COL2=`echo ${LINE} | awk -F ',' '{print $2}'`
    TGT_STR=${COL1},${COL2},
    sed -i -e "/^${TGT_STR}/d" ${TEMP_FILE05}_2.csv
done
