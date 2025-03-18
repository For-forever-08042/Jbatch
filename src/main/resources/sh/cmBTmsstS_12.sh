#!/bin/ksh
cd ${CM_APWORK_DATE}

RTN_VAL=`psql -q postgresql://${CONNECT_SD} >  ${CM_MYPRGID}.log  2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         INSERT INTO PS店表示情報@${CM_ORA_DBLINK_MD}
         SELECT * FROM PS店表示情報;
commit;
\q
EOF
`
echo ${?}
