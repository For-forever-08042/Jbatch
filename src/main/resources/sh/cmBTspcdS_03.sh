#!/bin/ksh
cd ${CM_APWORK_DATE}
cat ${FILE_NAME1} | awk -F, 'a[$1$2]++' > ${ERR_FILE_NAME}
echo ${?}
