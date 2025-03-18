###########################################
#  プログラム実行
###########################################
export CLASSPATH=${CM_JAVA_APBIN}/cmBTbmshJ.jar:${CM_JAVA_APBIN}/cmAOclibJ.jar:${CM_JAVA_APBIN}/lib/commons-dbcp-1.3.jar:${CM_JAVA_APBIN}/lib/commons-pool-1.5.4.jar:${CM_JAVA_APBIN}/lib/commons-codec-1.4.jar:${CM_JAVA_APBIN}/lib/commons-dbutils-1.3.jar:${CM_JAVA_APBIN}/lib/ojdbc8.jar:${CLASSPATH}
java -Xms1024m -Xmx1024m jp.co.mcc.nttdata.cmBTbmshJ.main.CMBTbmshMain ${ARG_BMSH_PRMFILE} ${INPUT_CSV_FILE_PATH} ${OPTION1} > /dev/null 2>&1
echo ${?}
