#!/bin/ksh
openssl enc -d -aes-128-ecb -in ${filename} -out ${TEMP_FILE1} -K "${txt1}" -iv "${txt2}" >/dev/null 2>&1