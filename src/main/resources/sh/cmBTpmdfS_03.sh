#!/bin/ksh
cd ${CM_APWORK_DATE}
#------------------------------------------
#実績ログ出力
#------------------------------------------
#結果ファイル総行数初期化
#FILE_CNT=`grep '' ${result_filename} |wc -l`
FILE_CNT=1
#OK結果数初期化
ok_cnt=0
#OKポイント数初期化
ok_point=0

# 結果ファイル総行数（ヘッダ行除く）を取得
cat ${result_filename} | while read line
do
    irai_kigyo=`echo ${line} | cut -d ',' -f 1 | cut -c 1-2`
    if  [ ${irai_kigyo} = "依頼" ] ; then
        continue
    else
        FILE_CNT=`expr ${FILE_CNT} + 1`
    fi
done

# 正常付与件数を取得
cat ${result_filename} | while read line
do
    result=`echo ${line} | cut -d ',' -f 18 | cut -c 1-2`
    point_zennen=`echo ${line} | cut -d ',' -f 10`
    point_tonen=`echo ${line} | cut -d ',' -f 11`
    point_mm0=`echo ${line} | cut -d ',' -f 13`
    point_mm1=`echo ${line} | cut -d ',' -f 14`
    point_mm2=`echo ${line} | cut -d ',' -f 15`
    point_mm3=`echo ${line} | cut -d ',' -f 16`
    if test ${result} = 10 ; then
        ok_point=`expr ${ok_point} + ${point_zennen} + ${point_tonen} + ${point_mm0} + ${point_mm1} + ${point_mm2} + ${point_mm3}`
        ok_cnt=`expr ${ok_cnt} + 1`
    fi
done
echo "    総付与ポイント数：${ok_point} P" > ${CM_JOBRESULTLOG}/${JBcs_G68003Z}.log
echo "    正常付与件数：${ok_cnt} 件" >> ${CM_JOBRESULTLOG}/${JBcs_G68003Z}.log

if test ${FILE_CNT} = ${ok_cnt}
then
    RESULT_SYMBOL='○'
else
    RESULT_SYMBOL='×'
fi

echo ${RESULT_SYMBOL}
