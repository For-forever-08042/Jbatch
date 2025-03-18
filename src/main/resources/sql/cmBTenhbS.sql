\set BDATE :1 -- バッチ処理日
\set UKETSUKEYMD :2 -- 受付日
\set KAISHACD :3 -- 会社コード
\set TENPOCD :4 -- 店舗コード
\set SHORINO :5 -- 処理番号
\set FUBICD :6 -- 不備理由コード
\set FILENAME :7 -- PDFファイル名
\set MYPRGID :8 -- プログラムID

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0

MERGE INTO
    TM会員申込書記入不備情報
USING (
    SELECT
        :UKETSUKEYMD AS 受付日
       ,:KAISHACD AS 会社コード
       ,:TENPOCD AS 店舗コード
       ,:SHORINO AS 処理番号
       ,CAST(:FUBICD AS TEXT)  AS 不備理由コード
       ,CAST(:FILENAME AS TEXT) AS ＰＤＦファイル
    FROM DUAL
) VAR
ON  (
    TM会員申込書記入不備情報.受付日 = VAR.受付日
AND TM会員申込書記入不備情報.会社コード = VAR.会社コード
AND TM会員申込書記入不備情報.店舗コード = VAR.店舗コード
AND TM会員申込書記入不備情報.処理番号 = VAR.処理番号
)
WHEN MATCHED THEN
    UPDATE
    SET
        不備理由コード = VAR.不備理由コード
       ,ＰＤＦファイル = VAR.ＰＤＦファイル
       ,バッチ更新日 = :BDATE
       ,最終更新日 = :BDATE
       ,最終更新日時 = SYSDATE()
       ,最終更新プログラムＩＤ = CAST(:MYPRGID AS TEXT)
WHEN NOT MATCHED THEN
    INSERT (
        受付日
       ,会社コード
       ,店舗コード
       ,処理番号
       ,不備理由コード
       ,ＰＤＦファイル
       ,削除フラグ
       ,バッチ更新日
       ,最終更新日
       ,最終更新日時
       ,最終更新プログラムＩＤ
    ) VALUES (
        VAR.受付日
       ,VAR.会社コード
       ,VAR.店舗コード
       ,VAR.処理番号
       ,VAR.不備理由コード
       ,VAR.ＰＤＦファイル
       ,0
       ,:BDATE
       ,:BDATE
       ,SYSDATE()
       ,CAST(:MYPRGID AS TEXT)
    )
;
