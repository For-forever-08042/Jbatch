#!/bin/ksh
ls -l ${CM_FILEWATSND}/${CRM_RENDO_FILE}.zip > ${CM_FILEWATSND}/R_ECSH_OK 2>&1
echo ${?}