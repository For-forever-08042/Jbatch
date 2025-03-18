#!/bin/ksh
cd ${CM_APWORK_DATE}
if test -f ${CM_APWORK_DATE}/${FinInFile}_${InFile}
then
    ls -1 ${CM_APWORK_DATE}/${FinInFile}_${InFile} | while read file
    do
        diffCnt=`diff ${file} ${OPTION1}/${ARG_FILENM} | wc -l`
        if test ${diffCnt} -eq 0
        then
            skipCnt=`expr ${skipCnt} + 1`
            result_skip_fname="limitedPoint_result_"`date +%Y%m%d%H%M%S`_skip_${skipCnt}
            echo "スキップしたファイル名："${ARG_FILENM} >> ${result_skip_fname}
            zenkai_filename=`echo ${file} | cut -c14-`
            echo "前回処理済みファイル名："${zenkai_filename} >> ${result_skip_fname}
            exit $((skipCnt))
        fi
    done
fi
echo ${?}
