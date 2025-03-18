\set SFILENAME :1                                                                                 --出力ファイルファイル名

\set ECHO none
\pset pager off
\pset footer off
\t
\pset format unaligned
\pset border 0


\o ./:SFILENAME

--過去の履歴とのデータ重複チェック(リカバリファイルのみ）
SELECT
    CONCAT(WA.親ＧＯＯＰＯＮ番号 , ',' ,
    WA.子ＧＯＯＰＯＮ番号 , ',' ,
    WA.削除フラグ , ',' ,
    WA.最終更新日)
FROM
    WSＧＯＯＰＯＮ番号紐付 WA,
    MSＧＯＯＰＯＮ番号紐付情報 MA
WHERE
    MA.子ＧＯＯＰＯＮ番号 = WA.子ＧＯＯＰＯＮ番号
AND MA.親ＧＯＯＰＯＮ番号 = WA.親ＧＯＯＰＯＮ番号
AND WA.削除フラグ = 0
;

\o

