
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

\o ./NG_EMERGENCY_SEND_ORDER_KK.CSV


SELECT
CONCAT('"' , A.企画ＩＤ , '"' , CHR(9) ,
'"' , A.バージョン , '"' )
FROM
 WSポイント付与条件O A
;
\o
