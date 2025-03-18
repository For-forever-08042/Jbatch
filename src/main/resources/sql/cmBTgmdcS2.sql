\set ECHO none
\t
\pset pager off
\pset footer off
\pset format unaligned
\pset border 0


\o ./:SFILENAME

--MSＧＯＯＰＯＮ番号紐付け情報反映
DELETE FROM MSＧＯＯＰＯＮ番号紐付情報 M
WHERE EXISTS ( SELECT 1 
                   FROM  WSＧＯＯＰＯＮ番号紐付 W 
                   WHERE M.子ＧＯＯＰＯＮ番号 = W.子ＧＯＯＰＯＮ番号 
                     AND W.削除フラグ=1 )
  AND NOT EXISTS ( SELECT 1 
                   FROM  WSＧＯＯＰＯＮ番号紐付 W 
                   WHERE M.子ＧＯＯＰＯＮ番号 = W.子ＧＯＯＰＯＮ番号 
                     AND W.削除フラグ=0 )
;

UPDATE MSＧＯＯＰＯＮ番号紐付情報 M
SET 親ＧＯＯＰＯＮ番号 = (
                  SELECT W.親ＧＯＯＰＯＮ番号
                   FROM  WSＧＯＯＰＯＮ番号紐付 W 
                   WHERE M.子ＧＯＯＰＯＮ番号 =  W.子ＧＯＯＰＯＮ番号
                     AND M.親ＧＯＯＰＯＮ番号 != W.親ＧＯＯＰＯＮ番号 
                     AND W.削除フラグ=0 )
WHERE EXISTS ( SELECT 1
                FROM  WSＧＯＯＰＯＮ番号紐付 W
                WHERE M.子ＧＯＯＰＯＮ番号 =  W.子ＧＯＯＰＯＮ番号
                  AND M.親ＧＯＯＰＯＮ番号 != W.親ＧＯＯＰＯＮ番号 
                  AND W.削除フラグ=0 )
;

INSERT INTO MSＧＯＯＰＯＮ番号紐付情報 (親ＧＯＯＰＯＮ番号 , 子ＧＯＯＰＯＮ番号)
SELECT W.親ＧＯＯＰＯＮ番号 , W.子ＧＯＯＰＯＮ番号
 FROM  WSＧＯＯＰＯＮ番号紐付 W
 WHERE W.削除フラグ=0
  AND  NOT EXISTS ( SELECT 1 
                     FROM  MSＧＯＯＰＯＮ番号紐付情報 M 
                     WHERE M.子ＧＯＯＰＯＮ番号 = W.子ＧＯＯＰＯＮ番号 )
;





