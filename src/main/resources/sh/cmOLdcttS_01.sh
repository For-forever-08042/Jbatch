#!/bin/ksh
rm ${CM_APWORK_DATE}/PCS_PT_*.tsv > /dev/null 2>&1
rm ${CM_APWORK_DATE}/PCS_TR_*.tsv > /dev/null 2>&1
rm ${CM_APWORK_DATE}/PCS_PT_*.dat > /dev/null 2>&1
rm ${CM_APWORK_DATE}/PCS_TR_*.dat > /dev/null 2>&1
rm ${CM_APWORK_DATE}/PCC?_?001.zip > /dev/null 2>&1
echo ${?}
