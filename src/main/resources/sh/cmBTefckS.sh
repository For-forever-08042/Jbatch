#!/bin/ksh
cd ${WORK_DIR}
unzip -Z ${CM_BKUPRESULT}/${BAT_YYYYMMDD}/*.zip | grep ${IN_FILE_NAME} > ${WORK_DIR}/${CM_MYPRGID}_${CUR_PID}
echo ${?}
