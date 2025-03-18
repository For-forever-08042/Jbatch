#!/bin/ksh
cd $CM_APWORK_DATE

cat ${TEMP_FILE04}.csv | while read LINE
do
    echo ${LINE} | awk -F ',' '{print $1","$2","$3","$5}'             >> ${CM_APWORK_DATE}/tmp_${S_GPMB_FILE}
    echo ${LINE} | awk -F ',' '{if($6 > 0){print $1","$2","$3","$5}}' >> ${CM_APWORK_DATE}/tmp_${R_GPMB_FILE}
    echo ${LINE} | awk -F ',' '{print $1","$2","$3","$4","$5}'        >> ${CM_APWORK_DATE}/tmp_${D_KHIF_FILE}
done

