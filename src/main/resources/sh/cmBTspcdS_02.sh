#!/bin/ksh
cd ${CM_APWORK_DATE}
zip -m ${ZIP_FILE_NAME1} ${OK_FILE_NAME} >/dev/null 2>&1
echo ${?}
