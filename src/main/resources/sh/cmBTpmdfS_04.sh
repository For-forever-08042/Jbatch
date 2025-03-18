#!/bin/ksh

maxCnt=000
if test -f ${CM_APWORK_DATE}/${FinInFile}_${ARG_FILENM}
then
    maxFile=`ls -r1 ${CM_APWORK_DATE}/${FinInFile}_${ARG_FILENM} | head -n +1`
    maxCnt=`basename ${maxFile} | cut -c 10-12`
fi
skipFileCnt=000`echo $(( 10#$maxCnt + 1 ))`
skipFileCnt="${skipFileCnt: -3}"

cp -p ${CM_FILENOWRCV}/${ARG_FILENM} ${CM_APWORK_DATE}/finished_${skipFileCnt}_${ARG_FILENM}
