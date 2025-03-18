#!/bin/ksh
ls -1 ${CM_BKUPRCV}/${SYS_YYYYMMDD_Y}/${FILE_NAME}_${SYS_YYYYMMDD_T}*.zip >> ${TEMP_FILE2} 2>/dev/null
echo ${?}
