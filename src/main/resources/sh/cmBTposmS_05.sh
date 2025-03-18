#!/bin/ksh
cd ${CM_APWORK_DATE}
##########################送信連動用ファイル作成
#####送信連動用ファイル作成(商品DNA向け)
ls -l ${CM_FILEWATSND}/${JOURNAL_RENKEI}.zip > ${CM_FILEWATSND}/${JRE_FILE_ID}_OK 2>&1
echo  ${?}
