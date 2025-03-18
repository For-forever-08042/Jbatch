#!/bin/ksh
ls -l ${CM_FILEWATSND}/${RESULT_FILE_NAME2}.zip > ${CM_FILEWATSND}/${R_FILE_ID2}_OK 2>&1
echo ${?}
