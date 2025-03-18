\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 

\o ./cmRJsql0S_03.utf

select concat(to_char(T.最終更新日時,'yyyymmdd'), ',', T.会員番号, ',', T.旧会員番号)
from cmuser.HSカード変更情報 T
where T.企業コード<>1020
and T.旧企業コード<>1020
and to_char(T.最終更新日時 ,'yyyymmdd') >= '20200520'
order by T.最終更新日時,T.会員番号,T.旧会員番号;

\o

