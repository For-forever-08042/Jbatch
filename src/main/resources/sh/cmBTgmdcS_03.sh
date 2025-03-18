#!/bin/ksh
cd $CM_APWORK_DATE

# ファイル内重複チェック
# 親ＧＯＯＰＯＮ番号、子ＧＯＯＰＯＮ番号 の重複をチェックする
cat ${TEMP_FILE01}.csv | sort -t $',' -k3,3 | awk -F, 'a[$1,$2]++ == 1' > ${OUT_ERR_FNAME}

# WSＧＯＯＰＯＮ番号紐付 登録レコードの準備
# 親ＧＯＯＰＯＮ番号、子ＧＯＯＰＯＮ番号、削除フラグ　が同一のレコードを除去する　※１行のみの状態にする
cat ${TEMP_FILE01}.csv | awk -F, 'a[$1,$2,$3]++ == 0' > ${TEMP_FILE01}.csv.tmp1

# 親ＧＯＯＰＯＮ番号、子ＧＯＯＰＯＮ番号 が同一で、削除フラグ＝0/1 のレコードを除去する(相殺分除去）
cat ${TEMP_FILE01}.csv.tmp1 | awk -F, 'a[$1,$2]++ == 1{print $1","$2}' > ${TEMP_FILE01}.csv.tmp2

cat ${TEMP_FILE01}.csv.tmp2 | while read LINE
do
    COL1=`echo ${LINE} | awk -F ',' '{print $1}'`
    COL2=`echo ${LINE} | awk -F ',' '{print $2}'`
    TGT_STR=${COL1},${COL2},
    sed -i -e "/^${TGT_STR}/d" ${TEMP_FILE01}.csv.tmp1
done

