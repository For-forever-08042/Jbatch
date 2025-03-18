#!/bin/ksh
ls -l ${CM_FILEWATSND}/${SPSS_RENDO_FILE}.zip > ${CM_FILEWATSND}/S_ECSH_OK 2>&1
echo ${?}