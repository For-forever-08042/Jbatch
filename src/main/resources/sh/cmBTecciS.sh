#!/bin/ksh
cd ${CM_APWORK_DATE}

join -a1 -t, ${OUT_FILE}_1 ${OUT_FILE}_2 > ${TEMP_FILE1}
awk 'BEGIN{ FS=","; RS="\r\n"; OFS=","; } {print $2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18}' ${TEMP_FILE1} > ${OUT_FILE}

rm -f ${TEMP_FILE1} > /dev/null 2>&1
rm -f ${OUT_FILE}_1 > /dev/null 2>&1
rm -f ${OUT_FILE}_2 > /dev/null 2>&1

echo ${?}
