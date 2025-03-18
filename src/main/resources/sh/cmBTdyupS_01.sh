###########################################
#  SQL実行
###########################################
cd ${WORK_DIR}
echo "\pset pager off"                     >${TEMP_FILE1}.sql
echo "\set AUTOCOMMIT off"                >>${TEMP_FILE1}.sql
echo "\set ON_ERROR_STOP true"            >>${TEMP_FILE1}.sql
echo "\set VERBOSITY verbose"             >>${TEMP_FILE1}.sql
echo -n "insert  into PSバッチ日付情報  " >>${TEMP_FILE1}.sql
echo -n "(バッチ処理年月日,  更新日時)  " >>${TEMP_FILE1}.sql
echo -n "values ('"                       >>${TEMP_FILE1}.sql
echo -n "${BAT_YYYYMMDD}"                 >>${TEMP_FILE1}.sql
echo    "', sysdate());"                  >>${TEMP_FILE1}.sql
echo "commit;"                            >>${TEMP_FILE1}.sql
echo "\q"                                 >>${TEMP_FILE1}.sql
psql -q postgresql://${CONNECT_SB} -f ${TEMP_FILE1}.sql >${TEMP_FILE2} 2>&1

echo ${?}
