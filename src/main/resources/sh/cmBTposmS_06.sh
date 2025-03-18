#!/bin/ksh
cd ${CM_APWORK_DATE}
#####送信連動用ファイル作成(SPSS向け)
ls -l ${CM_FILEWATSND}/${URIAGE_RENKEI_SPSS}.zip > ${CM_FILEWATSND}/${URS_FILE_ID}_OK 2>&1
echo  ${?}
