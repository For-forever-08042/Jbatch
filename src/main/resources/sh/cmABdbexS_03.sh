#!/bin/ksh
iconv -c -f  UTF-8 -t UTF-8 ${TEMP_FILE3} > ${TEMP_FILE4} 2>${TEMP_FILE5}
echo ${?}
