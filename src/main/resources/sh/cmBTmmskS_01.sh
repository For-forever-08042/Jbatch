#!/bin/ksh
ls -l ${CM_FILEWATSND}/${RESULT_FILE_NAME}.zip > ${CM_FILEWATSND}/${S_FILE_ID}_OK 2>&1
echo ${?}
