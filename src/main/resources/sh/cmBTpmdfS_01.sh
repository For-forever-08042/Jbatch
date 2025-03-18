#!/bin/ksh
gzip ${CM_APWORK_DATE}/${result_filename} > /dev/null 2>&1
echo ${?}
