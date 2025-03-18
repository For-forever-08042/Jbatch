ISLOOP=1
while test "${ISLOOP}" -ne 0
do

    if test $$ -ne `pgrep -fo $0`
    then
        if test -f ${CM_FILENOWRCV}/*_HC013*.zip
        then
            mv -f ${CM_FILENOWRCV}/*_HC013*.zip ${CM_APWORK}/ >/dev/null 2>&1
        fi
        sleep ${SLEEP_TIME}
    else
        if test -f ${CM_APWORK}/*_HC013*.zip
        then
            mv -f ${CM_APWORK}/*_HC013*.zip ${CM_FILENOWRCV}/ >/dev/null 2>&1
        fi
        ISLOOP=0
    fi
done
echo ${?}
