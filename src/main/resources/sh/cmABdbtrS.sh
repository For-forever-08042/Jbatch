#!/bin/ksh
cd ${WORK_DIR}
###########################################
#  SQLŽÀs
###########################################
echo "\pset footer off"                    > ${TEMP_FILE2}.sql
echo "\set AUTOCOMMIT off"                >> ${TEMP_FILE2}.sql
echo "\set ON_ERROR_STOP true"            >> ${TEMP_FILE2}.sql
echo "\set VERBOSITY verbose"             >> ${TEMP_FILE2}.sql
echo "TRUNCATE TABLE ${OPTION1};"         >> ${TEMP_FILE2}.sql
echo "COMMIT;"                            >> ${TEMP_FILE2}.sql
echo "\q"                                 >> ${TEMP_FILE2}.sql
RTN_VAL=`psql postgresql://${CONNECT_DB} -f ${TEMP_FILE2}.sql >> ${TEMP_FILE3} 2>&1`
echo ${?}
echo ${RTN_VAL}
