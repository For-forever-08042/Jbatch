#!/bin/ksh
ls -1 ls -1 ${CM_FILEWATRCV}/${FILE_NAME}_OK >> ${TEMP_FILE2} 2>/dev/null
echo ${?}
