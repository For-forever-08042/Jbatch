OPTIONS(ERRORS=0)
LOAD DATA
CHARACTERSET UTF8
INFILE 'cmBTgmdcS_ctl.tmp'
APPEND
INTO TABLE WSＧＯＯＰＯＮ番号紐付
FIELDS TERMINATED BY ','
TRAILING NULLCOLS
(    
      親ＧＯＯＰＯＮ番号
     ,子ＧＯＯＰＯＮ番号
     ,削除フラグ
     ,最終更新日
)
