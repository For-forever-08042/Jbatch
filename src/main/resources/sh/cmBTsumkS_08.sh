#!/bin/ksh
cd ${CM_APWORK_DATE}

mv KK00000004.lst KK00000004_1

#  改行コードをCRLFに変換
cat KK00000004_1 | sed 's/$/\r/g' > KK00000004
rm -f KK00000004_1

echo  ${?}
