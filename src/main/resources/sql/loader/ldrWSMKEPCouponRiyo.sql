--step2
TRUNCATE TABLE tmp_WSljdoN[|p;
\set ON_ERROR_STOP true
\COPY tmp_WSljdoN[|p (   O[|ïõhc ,éÆR[h ,lcéÆR[h ,XÜR[h ,WÔ ,æøÔ ,æøú ,|CgN[|éæR[h) FROM 'EcCouponData_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WSljdoN[|p;
INSERT INTO WSljdoN[|p (
     O[|ïõhc
    ,éÆR[h
    ,lcéÆR[h
    ,XÜR[h
    ,WÔ
    ,æøÔ
    ,æøú
    ,|CgN[|éæR[h
) 
SELECT 
     O[|ïõhc
    ,éÆR[h
    ,lcéÆR[h
    ,SUBSTR(XÜR[h, 11) AS XÜR[h
    ,WÔ
    ,æøÔ
    ,æøú
    ,|CgN[|éæR[h
FROM tmp_WSljdoN[|p;
TRUNCATE TABLE tmp_WSljdoN[|p;
