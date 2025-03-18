#!/bin/ksh
###########################################
#  プログラム実行
###########################################
#export CLASSPATH=${CM_JAVA_APBIN}/cmAOclibJ.jar:${CM_JAVA_APBIN}/lib/commons-dbcp-1.3.jar:${CM_JAVA_APBIN}/lib/commons-pool-1.5.4.jar:${CM_JAVA_APBIN}/lib/commons-codec-1.4.jar:${CM_JAVA_APBIN}/lib/commons-dbutils-1.3.jar:${CM_JAVA_APBIN}/lib/ojdbc5.jar:${CLASSPATH}
export CLASSPATH=${CM_WEBBAT}/cmAOclibJ.jar:${CM_JAVA_APBIN}/lib/commons-dbcp-1.3.jar:${CM_JAVA_APBIN}/lib/commons-pool-1.5.4.jar:${CM_JAVA_APBIN}/lib/commons-codec-1.4.jar:${CM_JAVA_APBIN}/lib/commons-dbutils-1.3.jar:${CM_JAVA_APBIN}/lib/ojdbc5.jar:${CLASSPATH}
java jp.co.mcc.nttdata.cmOLdcttJ.main.CMDCTransactionTransfer ${ARG_DCTT_PRMFILE} ${OPTION1} > /dev/null 2>&1
echo ${?}
