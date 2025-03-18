#!/bin/ksh
#------------------------------------------
#入力ファイルフォーマットチェック
#------------------------------------------
#for line in `cat ${InFile} | sed -e '/^,*$/d' -e '/^,/d'`
cat ${filename} | while read line
do
    cnt=`echo ${line} | grep , -o | wc -w`
    if test ${cnt} -ne 18 ; then
        exit ${Rtn_NG}
    fi
done

echo ${?}
