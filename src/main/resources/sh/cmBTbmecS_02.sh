#!/bin/ksh
export CLASSPATH=${CM_JAVA_APBIN}/cmBTbmecJ.jar:${CM_JAVA_APBIN}/cmAOclibJ.jar:${CM_JAVA_APBIN}/lib/postgresql-42.7.5.jar:${CM_JAVA_APBIN}/lib/commons-dbcp-1.3.jar:${CM_JAVA_APBIN}/lib/commons-pool-1.5.4.jar:${CM_JAVA_APBIN}/lib/commons-codec-1.4.jar:${CM_JAVA_APBIN}/lib/commons-dbutils-1.3.jar:${CM_JAVA_APBIN}/lib/ojdbc8.jar:${CLASSPATH}
 
java -Xms1024m -Xmx1024m jp.co.mcc.nttdata.cmBTbmecJ.main.CMBTbmecMain ${ARG_BNET_PRMFILE} ${OPTION1} > /dev/null 2>&1
echo ${?}