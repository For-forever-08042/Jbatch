#!/bin/ksh
###########################################
#  exp実行
###########################################
echo "--table=${OPTION1}${ADD_VALUE}"                          > ${TEMP_FILE1}
echo "--file=${WORK_DIR}/${OPTION3}${ADD_VALUE}${ADD_VALUE2}"  >> ${TEMP_FILE1}
iconv -f UTF-8 -t UTF-8 ${TEMP_FILE1} > ${TEMP_FILE2} 2>${TEMP_FILE5}
echo ${?}
