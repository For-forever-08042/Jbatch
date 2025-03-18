psql -q postgresql://${CONNECT_SD} <<eof
    \t
    \pset pager off
    \set ECHO none
    select to_char(to_date('${BAT_YYYYMMDD}','YYYYMMDD')-7,'YYYYMMDD') from dual;
eof
echo ${?}
