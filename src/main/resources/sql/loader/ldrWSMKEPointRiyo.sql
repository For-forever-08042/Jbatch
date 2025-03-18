--step2
TRUNCATE TABLE WSＭＫＥポイント利用;
\COPY WSＭＫＥポイント利用(  ＭＤ企業コード ,店舗コード ,電文ＳＥＱ番号 ,取引日時 ,取引番号 ,グーポン会員ＩＤ ,還元区分 ,ポイント数 ,送信日時 ,応答日時 ,送信結果コード ,還元結果フラグ ,変更ＩＤ ) FROM 'dekk_use_point_log_bmee_sjis.dat' WITH ( FORMAT csv, DELIMITER E'\t', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
