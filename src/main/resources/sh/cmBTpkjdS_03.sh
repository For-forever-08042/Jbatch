# 当月

cd ${CM_APWORK_DATE}

BAT_YYYYMM=`echo ${BAT_YYYYMMDD} | cut -c1-6`

# 前月
BAT_YYYYMMDD_LAST=`psql -q postgresql://${CONNECT_SD} <<EOF
         \t
         \pset pager off
         \set ECHO none
         SELECT TO_CHAR(ADD_MONTHS(TO_DATE(${BAT_YYYYMMDD}),-1), 'YYYYMMDD') FROM DUAL;
\q
EOF
`
BAT_YYYYMM_LAST=`echo ${BAT_YYYYMMDD_LAST} | cut -c1-6`

# 来月
BAT_YYYYMMDD_NEXT=`psql -q postgresql://${CONNECT_SD} <<EOF
         \t
         \pset pager off
         \set ECHO none
         SELECT TO_CHAR(ADD_MONTHS(TO_DATE(${BAT_YYYYMMDD}),+1), 'YYYYMMDD') FROM DUAL;
\q
EOF
`
BAT_YYYYMM_NEXT=`echo ${BAT_YYYYMMDD_NEXT} | cut -c1-6`

###########################################
#  プログラム実行
###########################################
RTN_VAL=`psql -q postgresql://${CONNECT_SD} > ${CM_MYPRGID}.log  2>&1 <<EOF
         \pset pager off
         \set AUTOCOMMIT off
         \set ON_ERROR_STOP true
         \set VERBOSITY verbose
         \set 1 ${BAT_YYYYMMDD} 
         \set 2 ${BAT_YYYYMM} 
         \set 3 ${BAT_YYYYMM_LAST}
         \set 4 ${BAT_YYYYMM_NEXT}
         \i ${CM_APSQL}/${CM_MYPRGID}.sql
commit;
\q
EOF
`
echo ${?}
