--step2
TRUNCATE TABLE tmp_WSljdwàzÏã;
\set ON_ERROR_STOP true
\COPY  tmp_WSljdwàzÏã (     lcéÆR[h ,XÜR[h ,d¶rdpÔ ,æøú ,wüú ,æøÔ ,O[|ïõhc ,æøæª ,Mú ,ú ,MÊR[h ,N»èpàz ,N»èp|Cgx¥àz ) FROM 'dekk_buy_price_log_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
TRUNCATE TABLE WSljdwàzÏã;
INSERT INTO WSljdwàzÏã (
     lcéÆR[h
    ,XÜR[h
    ,d¶rdpÔ
    ,æøú
    ,wüú
    ,æøÔ
    ,O[|ïõhc
    ,æøæª
    ,Mú
    ,ú
    ,MÊR[h
    ,N»èpàz
    ,N»èp|Cgx¥àz
) 
SELECT 
     lcéÆR[h
    ,XÜR[h
    ,d¶rdpÔ
    ,æøú
    ,wüú
    ,æøÔ
    ,O[|ïõhc
    ,æøæª
    ,Mú
    ,NVL(ú, '-1') AS ú
    , NVL(MÊR[h, '-1') AS MÊR[h
    ,N»èpàz
    ,N»èp|Cgx¥àz
FROM tmp_WSljdwàzÏã;
TRUNCATE TABLE tmp_WSljdwàzÏã;
