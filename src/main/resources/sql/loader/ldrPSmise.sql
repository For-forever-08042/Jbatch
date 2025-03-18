--step2
\set ON_ERROR_STOP true
\COPY  tmp_PS店表示情報( 企業コード , 店番号 , 開始年月日 , 終了年月日 , 漢字店舗名称 , 店舗カナ名称 , 店舗短縮名称 , dummy01 , dummy02 , dummy03 , dummy04 , dummy05 , dummy06 , dummy07 , dummy08 , dummy09 , dummy10 , dummy11 , 開店日 , 閉店日 , dummy14 , dummy15 , dummy16 , dummy17 , dummy18 , dummy19 , dummy20 , dummy21 , dummy22 , dummy23 , dummy24 , dummy25 , dummy26 , dummy27 , dummy28 , dummy29 , 調剤ＯＴＣ区分 , dummy31 , dummy32 , dummy33 , dummy34 , dummy35 , 連携用店番号 , dummy37 , dummy38 , dummy39 , dummy40 , dummy41 , dummy42 , dummy43 , dummy44 , dummy45 , 都道府県コード , dummy46 , dummy47 , dummy48 , ブロックコード , dummy49 , dummy50 , dummy51 , dummy52 , dummy53 , dummy54 , dummy55 , dummy56 , dummy57 , dummy58 , dummy59 , dummy60 , dummy61 , 判断日１ , dummy63 , dummy64 , dummy65 , dummy66 , dummy67 , dummy68 , dummy69 , dummy70 , dummy71 , dummy72 , dummy73 , dummy74 , dummy75 , dummy76 , dummy77 , dummy78 , dummy79 , dummy80 , dummy81 , dummy82 , dummy83 , 客層 , 面積区分 , 旧販社コード , dummy87 , dummy88 , dummy89 , dummy90 , dummy91 , dummy92 , dummy93 , dummy94 , dummy95 , dummy96 , dummy97 , dummy98 , dummy99 , dummy100 , dummy101 , dummy102 , dummy103 , dummy104 , dummy105 , dummy106 , dummy107 , dummy108 , dummy109 , dummy110 , dummy111 , dummy112 , dummy113 , dummy114 , dummy115 , dummy116 , dummy117 , dummy118 , dummy119 , dummy120 , dummy121 , dummy122 , dummy123 , dummy124 , dummy125 , dummy126 , dummy127 , dummy128 , dummy129 , dummy130 , dummy131 , dummy132 , dummy133 , dummy134 , dummy135 , dummy136 , dummy137 , dummy138 , dummy139 , dummy140 , dummy141 , dummy142 , dummy143 , dummy144 , dummy145 , dummy146 , dummy147 , dummy148 , dummy149 , dummy150 , dummy151 , dummy152 , dummy153 , dummy154 , dummy155 , dummy156 , dummy157 , dummy158 , dummy159 , dummy160 , dummy161 , dummy162 , dummy163 , dummy164 , dummy165 , dummy166 , dummy167 , dummy168 , dummy169 , dummy170 , dummy171 , dummy172 , dummy173 , dummy174 , dummy175 , dummy176 , dummy177 , dummy178 , dummy179 , dummy180 , dummy181 , dummy182 , dummy183 , dummy184 , dummy185 , dummy186 , dummy187 , dummy188 , dummy189 , dummy190 , dummy191 , dummy192 , dummy193 , dummy194 , dummy195 , dummy196 , dummy197 , dummy198 , dummy199 , dummy200 , dummy201 , dummy202 , dummy203 , dummy204 , dummy205 , dummy206 , dummy207 , dummy208 , dummy209 , dummy210 , dummy211 , dummy212 , dummy213 , dummy214 , dummy215 , dummy216 , クラスタ , 企業名称 , 企業短縮名称 , 企業名称カナ , 部名称 , 部短縮名称 , 部名称カナ , ゾーン名称 , ゾーン短縮名称 , ゾーン名称カナ , ブロック名称 , ブロック短縮名称 , ブロック名称カナ , バッチ更新日 , 最終更新日 , 最終更新日時 , 最終更新プログラムＩＤ  ) FROM 'MISE.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );

INSERT INTO  PS店表示情報(
企業コード,
店番号,
開始年月日,
終了年月日,
漢字店舗名称,
店舗カナ名称,
店舗短縮名称,
開店日,
閉店日,
調剤ＯＴＣ区分,
連携用店番号,
都道府県コード,
ブロックコード,
判断日１,
客層,
面積区分,
旧販社コード,
クラスタ,
企業名称,
企業短縮名称,
企業名称カナ,
部名称,
部短縮名称,
部名称カナ,
ゾーン名称,
ゾーン短縮名称,
ゾーン名称カナ,
ブロック名称,
ブロック短縮名称,
ブロック名称カナ,
バッチ更新日,
最終更新日,
最終更新日時,
最終更新プログラムＩＤ
) 
SELECT 
企業コード,
店番号,
開始年月日,
終了年月日,
漢字店舗名称,
店舗カナ名称,
店舗短縮名称,
TO_NUMBER(NULLIF(TRIM(CAST(開店日 AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(閉店日 AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(調剤ＯＴＣ区分 AS VARCHAR)),'')),
連携用店番号,
都道府県コード,
TO_NUMBER(NVL(NULLIF(TRIM(CAST(ブロックコード AS VARCHAR)),''), '0')),
TO_NUMBER(NULLIF(TRIM(CAST(判断日１ AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(客層 AS VARCHAR)),'')),
TO_NUMBER(NULLIF(TRIM(CAST(面積区分 AS VARCHAR)),'')),
TO_NUMBER(NVL(NULLIF(TRIM(CAST(旧販社コード AS VARCHAR)),''), '0')),
CASE NVL(NULLIF(TRIM(クラスタ),''),'#')
          WHEN 'A' THEN 1
          WHEN 'B' THEN 2
          WHEN 'C' THEN 3
          WHEN 'D' THEN 4
          WHEN 'Z' THEN 5
          WHEN '#' THEN 9
ELSE 
0
END,
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
' ',
:BATDATE,
:BATDATE,
SYSDATE(),
'cmBTmsstS'
FROM  tmp_PS店表示情報 where 企業コード in ('1010','1020','1030','1060');
TRUNCATE TABLE  tmp_PS店表示情報;
