#!/bin/ksh
cd ${CM_APWORK_DATE}
INPUT_FILE_NAME="${OPTION1%.*}"                                                 ### 入力ファイル名取得、拡張子なし
KAIINCHOUFUKU_FILE_NAME_DATE=${KAIINCHOUFUKU_FILE_NAME}_${SYS_YYYYMMDD}.csv     ### 出力ファイル名取得(日付付け)
# awkでファイル処理
awk -F, -v INPUT_FILE_NAME="${INPUT_FILE_NAME}" -v KINOUID="${OPTION2}" -v FIELD_NUMS="${OPTION3}" -v COUPON_NO="${OPTION4}" -v KAIINCHOUFUKU_FILE_NAME_DATE="${KAIINCHOUFUKU_FILE_NAME_DATE}" '
BEGIN {
    num_fields = split(FIELD_NUMS, fields, ",")
    if (system("test ! -e " KAIINCHOUFUKU_FILE_NAME_DATE) == 0) {
        print "グーポン番号,機能ID" > KAIINCHOUFUKU_FILE_NAME_DATE
    }
}
{
    key = ""
    for(i=1; i<=num_fields; i++) {
        key = key $fields[i] ","
    }
    if (seen[key]++ >= 1) {
        print $COUPON_NO "," KINOUID >> KAIINCHOUFUKU_FILE_NAME_DATE
    }
    else {
        print $0 > (INPUT_FILE_NAME "_unique.csv")
    }
}' ${INPUT_FILE_NAME}.csv

# 古いファイルを新しいファイルで置き換える
mv ${INPUT_FILE_NAME}_unique.csv ${INPUT_FILE_NAME}.csv > /dev/null 2>&1
