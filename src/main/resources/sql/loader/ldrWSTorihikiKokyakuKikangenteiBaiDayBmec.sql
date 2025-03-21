--step2
TRUNCATE TABLE tmp_WS取引顧客ワーク;
\set ON_ERROR_STOP true
\COPY tmp_WS取引顧客ワーク (  営業日付 ,会社コード ,店舗コード ,ノードＮＯ ,取引連番 ,発行店コード ,顧客コード ,今回ポイント ,サービス券発行ポイント ,サービス券発行枚数 ,来店ポイント ,誕生日ポイント ,入会月ポイント ,達成時加算ポイント ,調剤取引フラグ ,買上調整金額 ,サービス券発行金額 ,サービス券利用枚数 ,買上回数 ,買上回数ポイント ,前回ポイント ,単品ポイント ,通常付与Ｐ数 ,期間限定付与Ｐ数合計 ,期間限定付与Ｐ基準月 ,期間限定付与Ｐ数基準月０ ,期間限定付与Ｐ数基準月１ ,期間限定付与Ｐ数基準月２ ,期間限定付与Ｐ数基準月３ ,クーポンポイント ,特別ポイント ,特別ポイント回数 ,ランクポイント ,会員ランク ,会員番号登録区分 ,ランクポイント倍率 ,日別ポイント倍率 ,日別Ｐ倍率期間限定Ｐ数 ,日別Ｐ倍率期間限定失効期限 ,任意ポイント倍率 ,シニアポイント倍率 ,利用Ｐ内訳フラグ ,利用通常Ｐ数合計 ,利用通常Ｐ基準年度 ,利用通常Ｐ数基準年度前年 ,利用通常Ｐ数基準年度当年 ,利用通常Ｐ数基準年度翌年 ,利用期間限定Ｐ数合計 ,利用期間限定Ｐ基準月 ,利用期間限定Ｐ数基準月０ ,利用期間限定Ｐ数基準月１ ,利用期間限定Ｐ数基準月２ ,利用期間限定Ｐ数基準月３ ,利用期間限定Ｐ数基準月４ ,入会店舗コード ,入会旧販社コード ,期間内累計購入金額 ,前回累計購入金額 ,クーポン還元額 ,クーポン使用枚数 ,カード忘れフラグ ,カード忘れバーコード１ ,カード忘れバーコード２ ,元取引ノードno ,元取引日時 ,理由コード ,ポイント対象支払金額 ,ポイント非対象支払金額 ,オフラインフラグ ,ポイント照会ステータス ,ポイント更新ステータス ,還元区分 ,ポイント明細数 ) FROM 'S4141.dat' WITH ( FORMAT csv, DELIMITER ',', HEADER false, NULL '', QUOTE '"', ENCODING 'UTF8' );
--step3
INSERT INTO WS取引顧客ワーク
SELECT
    TO_NUMBER(TO_CHAR(営業日付, 'YYYYMMDD') ) AS 営業日付,
    会社コード,
    店舗コード,
    ノードＮＯ,
    取引連番,
    発行店コード,
    NVL(NULLIF(TRIM(顧客コード),''), '0') AS 顧客コード,
    今回ポイント,
    サービス券発行ポイント,
    サービス券発行枚数,
    来店ポイント,
    誕生日ポイント,
    入会月ポイント,
    達成時加算ポイント,
    調剤取引フラグ,
    買上調整金額,
    サービス券発行金額,
    サービス券利用枚数,
    買上回数,
    買上回数ポイント,
    前回ポイント,
    単品ポイント,
    通常付与Ｐ数,
    期間限定付与Ｐ数合計,
    期間限定付与Ｐ基準月,
    期間限定付与Ｐ数基準月０,
    期間限定付与Ｐ数基準月１,
    期間限定付与Ｐ数基準月２,
    期間限定付与Ｐ数基準月３,
    クーポンポイント,
    特別ポイント,
    特別ポイント回数,
    ランクポイント,
    会員ランク,
    会員番号登録区分,
    ランクポイント倍率,
    日別ポイント倍率,
    日別Ｐ倍率期間限定Ｐ数,
    日別Ｐ倍率期間限定失効期限,
    任意ポイント倍率,
    シニアポイント倍率,
    利用Ｐ内訳フラグ,
    利用通常Ｐ数合計,
    利用通常Ｐ基準年度,
    利用通常Ｐ数基準年度前年,
    利用通常Ｐ数基準年度当年,
    利用通常Ｐ数基準年度翌年,
    利用期間限定Ｐ数合計,
    利用期間限定Ｐ基準月,
    利用期間限定Ｐ数基準月０,
    利用期間限定Ｐ数基準月１,
    利用期間限定Ｐ数基準月２,
    利用期間限定Ｐ数基準月３,
    利用期間限定Ｐ数基準月４,
    入会店舗コード,
    入会旧販社コード,
    期間内累計購入金額,
    前回累計購入金額,
    クーポン還元額,
    クーポン使用枚数,
    カード忘れフラグ,
    カード忘れバーコード１,
    カード忘れバーコード２,
    元取引ノードno,
    元取引日時,
    理由コード,
    ポイント対象支払金額,
    ポイント非対象支払金額,
    オフラインフラグ,
    ポイント照会ステータス,
    ポイント更新ステータス,
    還元区分,
    ポイント明細数
FROM tmp_WS取引顧客ワーク;
TRUNCATE TABLE tmp_WS取引顧客ワーク;

