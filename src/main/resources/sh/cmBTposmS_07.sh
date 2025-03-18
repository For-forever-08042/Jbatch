#!/bin/ksh
cd ${CM_APWORK_DATE}
#####送信連動用ファイル作成(RetailCRM向け)
ls -l ${CM_FILEWATSND}/${URIAGE_Retail}.zip > ${CM_FILEWATSND}/${URE_FILE_ID}_OK 2>&1
echo  ${?}
