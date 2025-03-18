
\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0
SET orafce.nls_date_format = 'YYYY-MM-DD HH24:MI:SS'; 


\o ./NG_ALL_PROMOTION_KK.CSV

SELECT
CONCAT(CONCAT('"' , A.企画ＩＤ , '"' , CHR(9) , 
'"' , A.バージョン , '"' , CHR(9) , 
'"' , RPAD(A.確定区分,LENGTH(A.確定区分)) , '"' , CHR(9) ,  
'"' , RPAD(A.企業コード,LENGTH(A.企業コード)) , '"' , CHR(9) ,  
'"' , RPAD(A.発注番号,LENGTH(A.発注番号)) , '"' , CHR(9) ,  
'"' , RPAD(A.企画名称,LENGTH(A.企画名称)) , '"' , CHR(9) ,  
'"' , A.ポイントカテゴリ , '"' , CHR(9) ,  
'"' , A.組織指定区分 , '"' , CHR(9) ,  
'"' , A.部門指定区分 , '"' , CHR(9) ,  
'"' , A.カード種別指定区分 , '"' , CHR(9) ,  
'"' , A.会員指定区分 , '"' , CHR(9) ,  
'"' , A.倍率固定値区分 , '"' , CHR(9) ,  
'"' , A.計算対象外区分 , '"' , CHR(9) ,  
'"' , A.ポイント支払付与対象外区分 , '"' , CHR(9) ,  
'"' , A.買上高ポイント計算方法フラグ , '"' , CHR(9) ,  
'"' , A.ポイント付与会員修正フラグ , '"' , CHR(9) ,  
'"' , A.ポイント種別 , '"' , CHR(9) ,  
'"' , A.開始日 , '"' , CHR(9) ,  
'"' , A.終了日 , '"' , CHR(9) ,  
'"' , A.削除フラグ , '"' , CHR(9) ,  
'"' , TO_CHAR(A.送信日,'YYYY/MM/DD HH24:MI:SS') , '"' , CHR(9) ,  
'"' , TO_CHAR(A.作成日,'YYYY/MM/DD HH24:MI:SS') , '"' , CHR(9) ,  
'"' , TO_CHAR(COALESCE(A.確定日時,SYSDATE()),'YYYY/MM/DD HH24:MI:SS') , '"' , CHR(9) ,  
'"' , A.期間限定ポイント付与開始日 , '"' , CHR(9)) ,  
CONCAT('"' , A.期間限定ポイント有効期限 , '"' , CHR(9) ,  
'"' , A.タイムサービス時間帯開始時刻 , '"' , CHR(9) ,  
'"' , A.タイムサービス時間帯終了時刻 , '"' , CHR(9) ,  
'"' , A.全曜日フラグ , '"' , CHR(9) ,  
'"' , A.月曜日フラグ , '"' , CHR(9) ,  
'"' , A.火曜日フラグ , '"' , CHR(9) ,  
'"' , A.水曜日フラグ , '"' , CHR(9) ,  
'"' , A.木曜日フラグ , '"' , CHR(9) ,  
'"' , A.金曜日フラグ , '"' , CHR(9) ,  
'"' , A.土曜日フラグ , '"' , CHR(9) ,  
'"' , A.日曜日フラグ , '"' , CHR(9) ,    
'"' , A.期間限定ポイント計算方法区分 , '"')) 
FROM
 WSポイント付与条件O A 
;
\o
