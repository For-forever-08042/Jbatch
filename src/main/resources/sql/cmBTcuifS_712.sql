\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS';


/** 7.1.2. 制度DBからの取得 **/
TRUNCATE TABLE WM顧客ＲＣＶ;

INSERT INTO WM顧客ＲＣＶ( 顧客番号, ＧＯＯＰＯＮ番号, 外部認証ＩＤ, グローバル会員国コード, コーポレート会員フラグ) 
       SELECT 顧客番号
            , ＧＯＯＰＯＮ番号
            , MID
            , グローバル会員国コード
            , コーポレート会員フラグ
         FROM ( SELECT W.顧客番号
                     , C.ＧＯＯＰＯＮ番号
                     , NVL(G.外部認証ＩＤ,' ') AS MID
                     , S.グローバル会員国コード
                     , S.コーポレート会員フラグ
                     , ROW_NUMBER() OVER (PARTITION BY C.顧客番号                   --- 「顧客番号」ごとに、下記条件に沿って並べ替える
                                          ORDER     BY CASE C.サービス種別       --- ①「サービス種別」の値に応じて設定する判定値
                                                                         WHEN 1 THEN 1       ---   → C.サービス種別が 1 なら 1 を設定
                                                                         WHEN 4 THEN 2       ---   → C.サービス種別が 4 なら 2 を設定
                                                                         WHEN 3 THEN 3       ---   → C.サービス種別が 3 なら 3 を設定
                                                                         WHEN 2 THEN 4       ---   → C.サービス種別が 2 なら 4 を設定
                                                                         WHEN 5 THEN 5 END,      ---   → C.サービス種別が 5 なら 5 を設定
                                                       C.カードステータス,          --- ②「カードステータス」
                                                       C.発行年月日 DESC) as G_ROW  --- ③「発行年月日」
                  FROM MSカード情報   C
                     LEFT JOIN MS外部認証情報 G
                     ON C.サービス種別  = G.サービス種別 
                     AND C.会員番号      = G.会員番号     
                     AND 'L'             = G.外部認証種別 
                     , MS顧客制度情報 S
                     , WS顧客番号２     W
                     , PS会員番号体系 P
                 WHERE W.顧客番号      = S.顧客番号
                   AND W.顧客番号      = C.顧客番号
                   AND P.カード種別 not in ( 504, 505, 998 )
                   AND C.サービス種別  = P.サービス種別
                   AND C.会員番号     >= P.会員番号開始
                   AND C.会員番号     <= P.会員番号終了     )
        WHERE G_ROW =1
;

commit ;

