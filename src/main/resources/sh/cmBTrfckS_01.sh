#!/bin/ksh
ls -1 ${CM_FILEAFTRCV}/${FILE_NAME}_${SYS_YYYYMMDD_T}*.zip  > ${TEMP_FILE2} 2>/dev/null
ls -1 ${CM_FILEAFTRCV}/${FILE_NAME}_${SYS_YYYYMMDD_Y}*.zip >> ${TEMP_FILE2} 2>/dev/null
echo ${?}
