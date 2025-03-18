package jp.co.mcc.nttdata.batch.business.job.cmBTspmeS;

import jp.co.mcc.nttdata.batch.business.com.cmABaplwB.CmABaplwBServiceImpl;
import jp.co.mcc.nttdata.batch.business.service.cmBTspmeB.CmBTspmeBServiceImpl;
import jp.co.mcc.nttdata.batch.fw.com.basic.NtBasicTask;
import jp.co.mcc.nttdata.batch.fw.com.dto.MainResultDto;
import jp.co.mcc.nttdata.batch.fw.util.DateUtil;
import jp.co.mcc.nttdata.batch.fw.util.FileUtil;
import jp.co.mcc.nttdata.batch.fw.util.GZipUtil;
import jp.co.mcc.nttdata.batch.fw.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/*
        -------------------------------------------------------------------------------
            名称          ：  購買履歴作成データ連動シェル
            プログラムID  ：  cmBTspmeS

            【処理概要】
               「購買履歴作成（cmBTspmeB)」を起動するためのシェル。
               開始メッセージを出力し、「cmBTspmeB」を起動、
               終了メッセージを出力し、戻り値を返却。

            【引数説明】
               -sd         :  抽出対象範囲開始日付
               -ed         :  抽出対象範囲終了日付
               -DEBUG      :  デバッグモードでの実行（トレース出力機能が有効）
               -debug      :  デバッグモードでの実行（トレース出力機能が有効）

            【戻り値】
               10     ：  正常
               49     ：  警告
        -------------------------------------------------------------------------------
            稼働環境
              Red Hat Enterprise Linux 6

            改定履歴
              1.00 : 2021/08/11 NDBS.緒方：初版
              2.00 : 2021/09/17 SSI.上野：ファイル転送修正に伴い圧縮ファイル名変更
              3.00 : 2021/10/01 SSI.上野：(暫定)出力対象からEC取引データを除去
              4.00 : 2021/12/02 SSI. 張 ：レコード重複対応
        -------------------------------------------------------------------------------
          $Id:$
        -------------------------------------------------------------------------------
          Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION
        -------------------------------------------------------------------------------

 */

@Slf4j
@Component
public class CmBTspmeSTask extends NtBasicTask {

    @Autowired
    private CmABaplwBServiceImpl cmABaplwBServiceImpl;

    @Autowired
    private CmBTspmeBServiceImpl cmBTspmeBServiceImpl;


    //    ###########################################
    //                #  引数定義
    //    ###########################################

    String ARG_OPT1 = "-sd";       //抽出対象範囲開始日付
    String ARG_OPT2 = "-ed";       //抽出対象範囲終了日付
    String ARG_OPT3 = "-DEBUG";    //デバッグモードでの実施
    String ARG_OPT4 = "-debug";    //デバッグモードでの実施

    @Override
    public int taskExecuteCustom(String[] args) {

        //###########################################
        //#  プログラムIDを環境変数に設定
        //###########################################

        CM_MYPRGNAME = "購買履歴作成データ連動";

        //###########################################
        //#  開始メッセージをAPログに出力
        //###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).S(CM_MYPRGNAME));

        //    ###########################################
        //                #  定数定義
        //    ###########################################

        Rtn_OK = 10;
        Rtn_NG = 49;

        //# システム日時
        String SYS_YYYYMMDD = DateUtil.nowDateFormat("yyyyMMdd");

        String FILE_NAME = "^tmp_.{14}_KOBAIURI.csv$";

        //###########################################
        //#  稼動ディレクトリ決定
        //###########################################

        setCM_APWORK_DATE();

        if (!FileUtil.mkdir(CM_APWORK_DATE)) {
            // 作業ディレクトリファイル作成失敗
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("稼動ディレクトリ作成エラー").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  引数チェック
        //###########################################

        //        個数チェック
        //        set -- $*

        //        引数が3より多ければエラー

        if (args.length > 3) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [引数オーバー]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).M("異常終了").FE());
            return Rtn_NG;
        }

        //        初期化
        String OPT_SD = null;    //抽出対象範囲開始日付
        String OPT_ED = null;    //抽出対象範囲終了日付
        String OPT_DEB = null;   //デバッグ用文字列

        //        引数格納
        for (String arg : args) {
            //                引数が-sdの場合、
            if (arg.startsWith(ARG_OPT1)) {
                OPT_SD = arg;
                //                引数が-edの場合、
            } else if (arg.startsWith(ARG_OPT2)) {
                OPT_ED = arg;
            } else if (arg.startsWith(ARG_OPT3) || arg.startsWith(ARG_OPT4)) {
                OPT_DEB = arg;
            }
            //            指定以外の場合スキップ
        }

        //###########################################
        //# 任意引数をチェック
        //###########################################

        //        開始日付のみ指定
        if (StringUtils.isNotEmpty(OPT_SD) && StringUtils.isEmpty(OPT_ED)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [開始日指定あり　終了日指定なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //        終了日付のみ指定
        if (StringUtils.isEmpty(OPT_SD) && StringUtils.isNotEmpty(OPT_ED)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("引数エラー [開始日指定あり　終了日指定なし]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  購買履歴作成プログラム実行
        //###########################################
        MainResultDto RTN = cmBTspmeBServiceImpl.main(getExecuteBaseParam().add(OPT_SD).add(OPT_ED).add(OPT_DEB));
        if (RTN.exitCode != Rtn_OK) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("購買履歴作成エラー").FE());
            return Rtn_NG;
        }

        //###########################################
        //#  連動ファイル圧縮
        //###########################################

        ArrayList<String> FILE_NAME_LIST = FileUtil.findByRegex(CM_APWORK_DATE, FILE_NAME);

        String FILE_NAME1 = null;

        String ERR_FILE_NAME = null;

        if (CollectionUtils.isEmpty(FILE_NAME_LIST)) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮対象ファイルなし [" + FILE_NAME + "]").FI());
        } else {
            //            ###########################################
            //            #  購買履歴データファイルチェック
            //            ###########################################

            for (String filename : FILE_NAME_LIST) {
                int FILE_SIZE = FileUtil.readFile(filename).getBytes().length;
                if (FILE_SIZE != 0) {
                    FILE_NAME1 = new File(filename).getName().substring(4, 27);
                    String OK_FILE_NAME = FILE_NAME1 + ".csv";

                    //                     ###########################################
                    //                     #  レコード重複判断(重複しないレコード処理)
                    //                     ##########################################

                    FileUtil.writeFile(CM_APWORK_DATE + "/" + OK_FILE_NAME,awk(FileUtil.readFileByRegex(CM_APWORK_DATE, FILE_NAME), true));

                    //                    ###########################################
                    //                    #  購買履歴データファイル圧縮実行
                    //                    ###########################################

                    String ZIP_FILE_NAME1 = FILE_NAME1 + ".zip";

                    int compress = ZipUtil.zipFile(CM_APWORK_DATE + "/" + "tmp_" + ZIP_FILE_NAME1,
                            CM_APWORK_DATE + "/" + OK_FILE_NAME);
                    if (compress!=0) {
                        FileUtil.deleteFile(CM_APWORK_DATE + "/" + "tmp_" + ZIP_FILE_NAME1);
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮失敗[" + OK_FILE_NAME + "][" + "0" + "]").FE());
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                        return Rtn_NG;
                    }

                    //                     ###########################################
                    //                     #  レコード重複判断(重複したレコード処理)
                    //                     ##########################################

                    ERR_FILE_NAME = FILE_NAME1 + "_error.csv";

                    FileUtil.writeFile(CM_APWORK_DATE + "/" + OK_FILE_NAME,awk(FileUtil.readFileByRegex(CM_APWORK_DATE, FILE_NAME), false));

                    if (FileUtil.contentLength(CM_APWORK_DATE + "/" + ERR_FILE_NAME) != 0) {
                        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("重複レコードあり[" + ERR_FILE_NAME + "]"));

                        //                        ###########################################
                        //                        #  重複したレコードをエラーファイル圧縮実行
                        //                        ###########################################

                        String ZIP_FILE_NAME2 = FILE_NAME1 + "_error.zip";

                        int compress1 = ZipUtil.zipFile(CM_APWORK_DATE + "/" + ZIP_FILE_NAME2,
                                CM_APWORK_DATE + "/" + ERR_FILE_NAME);

                        if (compress1!=0) {
                            FileUtil.deleteFile(CM_APWORK_DATE + "/" + "tmp_" + ZIP_FILE_NAME1);
                            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("圧縮失敗[" + ZIP_FILE_NAME2 + "][" + "0" + "]").FE());
                            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
                            return Rtn_NG;
                        }

                    }


                }

            }

        }

        //###########################################
        //#  元ファイル削除
        //###########################################

        boolean deleteFile = FileUtil.deleteFileByRegx(CM_APWORK_DATE, FILE_NAME);
        if (!deleteFile) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("元ファイル削除失敗  [" + FILE_NAME + "][" + "0" + "]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //###########################################
        //#  元エラーファイル削除
        //###########################################
        if (FileUtil.deleteFile(CM_APWORK_DATE + "/" + ERR_FILE_NAME)!=0) {
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).M("元エラーファイル削除失敗  [" + FILE_NAME1 + "][" + "0" + "]").FE());
            cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME).FE());
            return Rtn_NG;
        }

        //            ###########################################
        //            #  終了メッセージをAPログに出力
        //            ###########################################

        cmABaplwBServiceImpl.main(getExecuteBaseParam().P(CM_MYPRGID).E(CM_MYPRGNAME));

        return Rtn_OK;
    }

    private String awk(String s,boolean b) {

        ArrayList<String[]> filedsList = new ArrayList<>();

        String[] lines = s.split("\n");
        for (String line : lines) {
            String[] fields = line.split(",");
            filedsList.add(fields);
        }

        ArrayList<String[]> copyFiledsList = new ArrayList<>();
        ArrayList<String[]> copyFiledTmpList = new ArrayList<>();
        for (String[] fileds : filedsList) {
            boolean flag = true;
            for (String[] copyFileds : copyFiledsList) {
                String[] tmpFileds;
                String[] copyTmpFileds;
                if (fileds.length > 7) {
                    tmpFileds = Arrays.copyOf(fileds, 7);
                } else {
                    tmpFileds = fileds;
                }
                if (copyFileds.length > 7) {
                    copyTmpFileds = Arrays.copyOf(copyFileds, 7);
                } else {
                    copyTmpFileds = copyFileds;
                }
                if (Arrays.equals(tmpFileds, copyTmpFileds)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                copyFiledsList.add(fileds);
            } else {
                copyFiledTmpList.add(fileds);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (b){
            for (String[] strings : copyFiledsList) {
                for (int i = 0; i < strings.length; i++) {
                    if (i == strings.length - 1) {
                        stringBuilder.append(strings[i]).append("\n");
                    } else {
                        stringBuilder.append(strings[i]).append(",");
                    }
                }
            }
        }else {
            for (String[] strings : copyFiledTmpList) {
                for (int i = 0; i < strings.length; i++) {
                    if (i == strings.length - 1) {
                        stringBuilder.append(strings[i]).append("\n");
                    } else {
                        stringBuilder.append(strings[i]).append(",");
                    }
                }
            }
        }

        return stringBuilder.toString();

    }


}
