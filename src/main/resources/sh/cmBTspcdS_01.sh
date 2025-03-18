#!/bin/ksh
cd ${CM_APWORK_DATE}
cat ${FILE_NAME1} | awk -F, '!a[$1$2]++' > ${OK_FILE_NAME}
echo ${?}
