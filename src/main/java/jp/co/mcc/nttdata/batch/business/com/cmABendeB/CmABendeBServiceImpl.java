package jp.co.mcc.nttdata.batch.business.com.cmABendeB;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.business.com.cmABfuncL.CmABfuncLServiceImpl;
import jp.co.mcc.nttdata.batch.fw.util.Base64Utils;
import jp.co.mcc.nttdata.batch.fw.util.ConvertUtil;
import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/******************************************************************************/
/*   プログラム名   ：mcABendeB                                               */
/*                                                                            */
/*   共通関数：                                                               */
/*      C_EncDec_AES   : データ暗号復号処理(AES-128)                          */
/*      C_EncDec_DES   : データ暗号化処理(DES)                                */
/*                                                                            */
/*----------------------------------------------------------------------------*/
/*   稼働環境                                                                 */
/*      Red Hat Enterprise Linux 6（64bit）                                   */
/*      (文字コード ： UTF8)                                                  */
/*----------------------------------------------------------------------------*/
/*   改定履歴                                                                 */
/*      1.00 :  2021/10/11 上野   ： 初版                                     */
/*      2.00 :  2022/04/06 上野   ： CF-648　DES復号異常改                    */
/*     40.00 :  2022/10/25 張シン ： MCCM初版                                 */
/*----------------------------------------------------------------------------*/
/*  $Id:$                                                                     */
/*----------------------------------------------------------------------------*/
/*  Copyright (C) 2021 NTT DATA BUSINESS SYSTEMS CORPORATION                  */

/******************************************************************************/
@Service
@Slf4j
public class CmABendeBServiceImpl extends CmABfuncLServiceImpl implements CmABendeBService {
    /*      関数単位にトレース出力要否が設定できるように定義                      */
    int dbg_encdec_aes = 1;         /* データ暗号復号処理(AES-128)                */
    int dbg_encdec_des = 1;         /* データ暗号復号処理(DES)                    */
    int dbg_encdec_pid = 1;         /* 会員番号復号補正処理                  */
    int dbg_encdec_hash = 1;         /* ハッシュ化処理                  */
    /*----------------------------------------------------------------------------*/
    /*  定数                                                                      */
    /*----------------------------------------------------------------------------*/
    /* 暗号化復号フラグ */
    int C_INFLG_ENCODE = 1;     /* 暗号                                       */
    int C_INFLG_DECODE = 2;    /* 復号                                       */
    /* 関数戻り値           */
    int C_Ret_1 = 1;
    int C_Ret_2 = 2;
    int C_Ret_3 = 3;
    int C_Ret_4 = 4;
    int C_Ret_5 = 5;
    int C_Ret_6 = 6;

/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： C_EncDec_Pid                                                    */
    /*                                                                            */
    /*  書式                                                                      */
    /*  int     C_EncDec_Pid( int endeFlg, char *pattern,                         */
    /*                       const unsigned char *indata, int indataLen,          */
    /*                       unsigned char *outdata, int *outdataLen )            */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*          データ暗号復号化処理                                              */
    /*          (DES/ECB/引数、Base64）                                           */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*          int                 endeFlg     ： (I)暗号復号フラグ              */
    /*                                                1: 暗号                     */
    /*                                                2: 復号                     */
    /*          char                *pattern    ： (I)暗号化パータン              */
    /*                                                MP1: MK-POS 電文ファイル    */
    /*                                                MP2: MK-POS 電文ファイル以外*/
    /*                                                SD: 商品DNA、SPSS           */
    /*                                                GL: 群豊                    */
    /*          const unsigned char *indata     ： (I)変換元文字列                */
    /*          unsigned char       *outdata    ： (O)変換後文字列                */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0       ： 正常                                               */
    /*              1       ： 異常(暗号復号フラグ不正)                           */
    /*              2～5    ： 異常(openssl関数エラー)                            */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_EncDec_Pid(int endeFlg, String pattern, String indata, StringDto outdata) {
        int rtn_cd;                      /* 関数戻り値                      */
        IntegerDto outdataLen = new IntegerDto();                  /* 変換後文字列長さ                */
        String key_buf = null;                /* 暗号化キー                      */
        int keyLen;                      /* 暗号化キー長さ                  */
        String iv_buf = null;                 /* IV                              */
        int ivLen;                       /* IV長さ                          */
        int indataEncLen;                /* 暗号化用変換元文字列長さ        */
        int indataLen;                   /* 復号化用変換元文字列長さ        */
        int padding;                     /* padding                         */
        StringDto wk_data_buf = new StringDto();           /* ワーク化用変換元文字列          */

        indataLen = strlen(indata);
//        memset(wk_data_buf, 0x00, sizeof(wk_data_buf));

        if (endeFlg == 1) {
            /* 暗号化文字は8の倍数に補正 */
            if (indataLen % 8 == 0) {
                indataEncLen = indataLen;
            } else {
                indataEncLen = indataLen + 8 - (indataLen % 8);
            }
            indataLen = indataEncLen;
        }

//        memset(outdata, 0x00, sizeof(outdata));
        outdataLen.arr = 0;

        /* MK-POS 電文ファイル */
        if (strcmp(pattern, "MP1") == 0) {
            /* 暗号化キー設定 */
            key_buf = strcpy(key_buf, "mkGoopon");
            keyLen = strlen(key_buf);
            padding = 0;

            rtn_cd = C_EncDec_DES(endeFlg, key_buf, keyLen, indata, indataLen, wk_data_buf, outdataLen, padding);

            if (rtn_cd != C_const_OK) {
                if (dbg_encdec_pid == 1) {
                    C_DbgMsg("C_EncDec_Pid : C_EncDec_DES %s\n", "ERROR");
                }
                return C_const_NG;
            }



            /* MK-POS 電文ファイル 以外 */
        } else if (strcmp(pattern, "MP2") == 0) {
            /* 暗号化キー設定 */
            key_buf = strcpy(key_buf, "3uhc29y1");
            keyLen = strlen(key_buf);
            padding = 0;

            rtn_cd = C_EncDec_DES(endeFlg, key_buf, keyLen, indata, indataLen, wk_data_buf, outdataLen, padding);

            if (rtn_cd != C_const_OK) {
                if (dbg_encdec_pid == 1) {
                    C_DbgMsg("C_EncDec_Pid : C_EncDec_DES %s\n", "ERROR");
                }
                return C_const_NG;
            }


            /* 商品DNA、SPSS */
        } else if (strcmp(pattern, "SD") == 0) {
            /* 暗号化キー設定 */
            byte[] key_buf_by = new byte[8];
            key_buf_by[0] = -63;
            key_buf_by[1] = 122;
            key_buf_by[2] = 16;
            key_buf_by[3] = 121;
            key_buf_by[4] = -62;
            key_buf_by[5] = -39;
            key_buf_by[6] = -104;
            key_buf_by[7] = -17;
            padding = 1;
            key_buf = ConvertUtil.bytesToHexString(key_buf_by);
            keyLen = strlen(key_buf);
            rtn_cd = C_EncDec_DES(endeFlg, key_buf, keyLen, indata, indataLen, wk_data_buf, outdataLen, padding);

            if (rtn_cd != C_const_OK) {
                if (dbg_encdec_pid == 1) {
                    C_DbgMsg("C_EncDec_Pid : C_EncDec_DES %s\n", "ERROR");
                }
                return C_const_NG;
            }

            /* 群豊 */
        } else if (strcmp(pattern, "GL") == 0) {
            /* 暗号化キー設定 */
            key_buf = strcpy(key_buf, "UhEUJ738dD7kdf83");
            iv_buf = strcpy(iv_buf, "ikeJ7d5KSd2lvUTk");
            keyLen = strlen(key_buf);
            ivLen = strlen(iv_buf);

            rtn_cd = C_EncDec_AES(endeFlg, key_buf, keyLen, iv_buf, ivLen, indata, indataLen, wk_data_buf, outdataLen);

            if (rtn_cd != C_const_OK) {
                if (dbg_encdec_pid == 1) {
                    C_DbgMsg("C_EncDec_Pid : C_EncDec_AES %s\n", "ERROR");
                }
                return C_const_NG;
            }


        } else if (strcmp(pattern, "NO") == 0) {
            strncpy(wk_data_buf, indata, indataLen);
            int i = 0;
            outdataLen.arr = strlen(wk_data_buf);
            for (i = 0; i < strlen(wk_data_buf); i++) {
                if (wk_data_buf.charAt(i) >= '0' && wk_data_buf.charAt(i) <= '9') {
                    continue;
                } else {
                    outdataLen.arr = i;
                    break;
                }
            }
            C_DbgMsg("outdataLen=[%d]\n", outdataLen);
            strncpy(outdata, wk_data_buf, outdataLen.arr);

        } else {

            return C_const_NG;
        }

        if (endeFlg == 2) {
            int i = 0;
            outdataLen.arr = strlen(wk_data_buf);
            for (i = 0; i < strlen(wk_data_buf); i++) {
                if (wk_data_buf.charAt(i) >= '0' && wk_data_buf.charAt(i) <= '9') {
                    continue;
                } else {
                    outdataLen.arr = i;
                    break;
                }
            }
            C_DbgMsg("outdataLen=[%d]\n", outdataLen);
            strncpy(outdata, wk_data_buf, outdataLen.arr);

            rtn_cd = C_CorrectMemberNo(outdata);
            if (rtn_cd != C_const_OK) {
                if (dbg_encdec_pid == 1) {
                    C_DbgMsg("C_EncDec_Pid : C_CorrectMemberNo %s\n", "ERROR");
                }
                return C_const_NG;
            }
        } else {
            strcpy(outdata, wk_data_buf);
        }

        if (dbg_encdec_pid == 1) {
            C_DbgMsg("C_EncDec_Pid : return PidCD[%s]\n", outdata);
        }
        return C_const_OK;
    }

    private byte[] getKeyByte(String key) {
        if ("C17A1079C2D998EF".equals(key)) {
            return ConvertUtil.hexStringToByte(key);
        } else {
            return key.getBytes();
        }
    }
/******************************************************************************/
    /*                                                                            */
    /*  関数名 ： C_EncDec_DES                                                    */
    /*                                                                            */
    /*  書式                                                                      */
    /*  int     C_EncDec_DES( int endeFlg, const unsigned char *key, int keyLen,  */
    /*                       const unsigned char *iv,  int ivLen,                 */
    /*                       const unsigned char *indata, int indataLen,          */
    /*                       unsigned char *outdata, int *outdataLen              */
    /*                       int padding = 0 )                                    */
    /*                                                                            */
    /*  【説明】                                                                  */
    /*          データ暗号復号化処理                                              */
    /*          (DES/ECB/引数]、Base64）                                          */
    /*                                                                            */
    /*  【引数】                                                                  */
    /*          int                 endeFlg     ： (I)暗号復号フラグ              */
    /*          const unsigned char *key        ： (I)変換Key                     */
    /*          int                 keyLen      ： (I)変換Key長さ                 */
    /*          const unsigned char *indata     ： (I)変換元文字列                */
    /*          int                 indataLen   ： (I)変換元文字列長さ            */
    /*          unsigned char       *outdata    ： (O)変換後文字列                */
    /*          int                 *outdataLen ： (O)変換後文字列長さ            */
    /*          int                 padding     ： (I)パディング(デフォルト0)     */
    /*                                                0:ZEROpadding               */
    /*                                                ?:padding                   */
    /*                                                                            */
    /*  【戻り値】                                                                */
    /*              0       ： 正常                                               */
    /*              1       ： 異常(暗号復号フラグ不正)                           */
    /*              2～5    ： 異常(openssl関数エラー)                            */
    /*                                                                            */

    /******************************************************************************/
    @Override
    public int C_EncDec_DES(int endeFlg, String key, int keyLen, String indata, int indataLen, StringDto outdata,
                            IntegerDto outdataLen, int padding) {

        if (dbg_encdec_des == 1) {
            C_DbgMsg("C_EncDec_DES : start[%d]1:encode 2:decode\n", endeFlg);
        }

        if (endeFlg != C_INFLG_ENCODE && endeFlg != C_INFLG_DECODE) {
            /* 暗号復号フラグ不正 */
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_DES : endeFlg[%d]\n", endeFlg);
            }
            return C_Ret_1;
        }

        Cipher cipher = null;
        try {
            if (padding == 0) {
                cipher = Cipher.getInstance("DES/ECB/NoPadding");
            } else {
                cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        /* 領域確保 */
        StringDto intext = new StringDto(indataLen + 1);
        StringDto outtext = new StringDto(indataLen * 2);
        StringDto outtext64 = new StringDto(indataLen * 2);

        /* 変換元文字列 */
        memset(intext, 0x00, indataLen + 1);
        memcpy(intext, indata, indataLen);

        memset(outtext, 0x00, indataLen * 2);
        memset(outtext64, 0x00, indataLen * 2);


        if (dbg_encdec_des == 1) {
//            printHex("Key", key, keyLen);
            printHex(key);
        }

        SecretKeySpec ctx = null;
        ctx = new SecretKeySpec(getKeyByte(key), "DES");
        if (ctx == null) {
            if (dbg_encdec_des == 1) {
                C_DbgMsg("C_EncDec_DES : EVP_CIPHER_CTX_new_ERROR%s\n", "");
            }
            /* メモリ解放 */
            freeALL(intext, outtext, outtext64, null);
            return C_Ret_3;
        }

        //byte[]doFinal（byte[]content）：contentに対して暗号化操作を完了し、cipher.init初期化時に使用する復号モードであれば、復号操作である.
//暗号化されたバイト配列を返します。直接new String（byte[]bytes）が文字化けしている場合は、BASE 64を使用して可視文字列に変換したり、16進文字に変換したりできます
//        byte[] encrypted = cipher.doFinal(convertnullPaddingByte(content, null, 0));
//
//        String result = Base64.getEncoder().encodeToString(encrypted);


        /**********************/
        /* 暗号処理／復号処理 */
        /**********************/
        if (endeFlg == C_INFLG_ENCODE) {
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : %s start\n", "decode");
            }


            try {
                cipher.init(Cipher.ENCRYPT_MODE, ctx);
            } catch (InvalidKeyException e) {
                cipher = null;
                log.error(ExceptionUtil.getExceptionMessage(e));
            }


            if (cipher == null) {
                if (dbg_encdec_des == 1) {
                    C_DbgMsg("C_EncDec_DES : EVP_EncryptInit_ex_ERROR[%s]\n",
                            "EncryptInit failed");
                }
                /* メモリ解放 */
                freeALL(intext, outtext, outtext64, null);

                return C_Ret_4;
            }
            /* 暗号化 */
            byte[] encrypted = null;
            try {
                encrypted = cipher.doFinal(convertnullPaddingByte(intext.arr, null, 0));
            } catch (IllegalBlockSizeException e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            } catch (BadPaddingException e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
            /* パディング有効 */
            /* 2022/10/25 MCCM初版 MOD START */
            //EVP_CIPHER_CTX_set_padding(ctx, 1);
//            EVP_CIPHER_CTX_set_padding(ctx, padding);
            /* 2022/10/25 MCCM初版 MOD END */

            /* 暗号化 */
            if (encrypted == null) {
                if (dbg_encdec_des == 1) {
                    C_DbgMsg("C_EncDec_DES : EVP_EncryptUpdate_ERROR[%s]\n",
                            "Encrypt failed");
                }
                /* メモリ解放 */
                freeALL(intext, outtext, outtext64, null);

                return C_Ret_5;
            }

//            /* パディング有効(デフォルト)の場合、最終データ暗号化*/
//            if (encrypted == null){
//                if (dbg_encdec_des == 1) {
//                    C_DbgMsg("C_EncDec_DES : EVP_EncryptFinal_ex_ERROR[%s]\n",
//                            "EncryptFinal failed");
//                }
//                /* メモリ解放 */
//                freeALL(intext, outtext, outtext64, null);
//
//                return C_Ret_6;
//            }
            int outLen = encrypted.length;
            outtext.arr = new String(encrypted);
            if (dbg_encdec_des == 1) {
                C_DbgMsg("C_EncDec_DES : outtext[%s]\n", outtext);
                C_DbgMsg("C_EncDec_DES : outLen[%d]\n", outLen);
            }

            /* base64変換処理 →base64 */
            String base64_wk = Base64Utils.encode(encrypted);
            strcpy(outtext64, base64_wk);
            int outLen64 = strlen(outtext64);

            if (dbg_encdec_des == 1) {
                C_DbgMsg("C_EncDec_DES : base64_wk[%s]\n", base64_wk);
                C_DbgMsg("C_EncDec_DES : outtext64[%s]\n", outtext64);
                C_DbgMsg("C_EncDec_DES : outLen64[%d]\n", outLen64);
            }

            /* 変換後文字列 */
            strcpy(outdata, outtext64);
            outdataLen.arr = outLen64;

        } else if (endeFlg == C_INFLG_DECODE) {
            /* 復号 */
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_DES : %s start\n", "decode");
            }

            /* base64変換処理 base64→ */
            /* 2022/04/06 null文字認識のため引数追加        */
            /* base64_wk = base64decode(intext, indataLen); */
            /* strcpy((char*)outtext64, (char*)base64_wk);  */
            /* outLen64 = strlen((const char*)outtext64);   */

            byte[] base64_wk = Base64Utils.decode(intext.arr);
            int outLen64 = base64_wk.length;
            // memcpy(outtext64, base64_wk, outLen64);
            outtext64.arr = new String(base64_wk);
            if (dbg_encdec_des == 1) {
                C_DbgMsg("C_EncDec_DES : outtext64[%s]\n", outtext64);
                C_DbgMsg("C_EncDec_DES : outLen64[%d]\n", outLen64);
            }

            /* 復号 */
            //cipherを初期化します。復号モードを使用する
            try {
                cipher.init(Cipher.DECRYPT_MODE, ctx);
            } catch (InvalidKeyException e) {
                cipher = null;
                log.error(ExceptionUtil.getExceptionMessage(e));
            }

            if (cipher == null) {
                if (dbg_encdec_des == 1) {
                    C_DbgMsg("C_EncDec_DES : EVP_DecryptInit_ex_ERROR[%s]\n",
                            "DecryptInit failed");
                }
                /* メモリ解放 */
                freeALL(intext, outtext, outtext64);

                return C_Ret_4;
            }

            /* パディング無効 */
            /* 2022/10/25 MCCM初版 DEL START */
            //EVP_CIPHER_CTX_set_padding(ctx, 0);
            /* 2022/10/25 MCCM初版 DEL END */

            /* 復号 */
            byte[] decrypted = null;
            try {
                decrypted = cipher.doFinal(base64_wk);
            } catch (IllegalBlockSizeException e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            } catch (BadPaddingException e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
            if (decrypted == null) {
                if (dbg_encdec_des == 1) {
                    C_DbgMsg("C_EncDec_DES : EVP_DecryptUpdate_ERROR[%s]\n",
                            "Decrypt failed");
                }
                /* メモリ解放 */
                freeALL(intext, outtext, outtext64);

                return C_Ret_5;
            }
            outtext.arr = new String(decrypted).replaceAll("[\0]", "");
            /* 変換後文字列 */
            strcpy(outdata, outtext);
            outdataLen.arr = strlen(outtext);
        }

        if (dbg_encdec_des == 1) {
            C_DbgMsg("C_EncDec_DES : outdata[%s]\n", outdata);
            C_DbgMsg("C_EncDec_DES : outdataLen[%d]\n", outdataLen);
        }

        /* メモリ解放 */
        freeALL(intext, outtext, outtext64);

        if (dbg_encdec_des == 1) {
            C_DbgMsg("C_EncDec_DES : %s\n", "end");
        }
        return 0;
    }

    private void freeALL(StringDto... intext) {
        for (StringDto item : intext) {
            if (item != null) {
                item.arr = null;
            }
        }
    }

    /**
     * メソッド名   ：byte変換(null詰め)
     * メソッド説明 ：文字列のbyteサイズが8の倍数となるように変換します（不足分をnull(0)で詰める）
     * 備考         ：
     *
     * @param str    文字列
     * @param encode 文字コード
     * @return byte[] 変換後バイト配列
     * @throws Exception エンコーディング例外
     */
    private byte[] convertnullPaddingByte(String str, String encode, int padding) {
        byte[] nullPadB = null;
        try {
            int multiple = 8;
            byte[] b = null;
            if (null == encode || encode.isEmpty()) {
                b = str.getBytes();
            } else {
                str.getBytes(encode);
            }
            int mod = b.length % multiple;
            // 8の倍数となっている場合はそのまま返却
            if (mod == 0) {
                return b;
            }

            // 8の倍数でnull(0)詰めした配列へ詰めかえる
            nullPadB = new byte[b.length + (multiple - mod)];
            Arrays.fill(nullPadB, (byte) padding);
            for (int i = 0; i < b.length; i++) {
                nullPadB[i] = b[i];
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return nullPadB;
    }

    private void printHex(String str) {
        System.out.println(ConvertUtil.bytesToHexString(str.getBytes()));
    }

    @Override
    public int C_EncDec_AES(int endeFlg, String key, int keyLen, String iv, int ivLen, String indata, int indataLen,
                            StringDto outdata, IntegerDto outdataLen) {

        int outLen;             /* 暗号復号後文字列長さ          */
        int outLen64;           /* base64変換後文字列長さ        */
        int out1, out2;         /* 暗号復号化ワーク              */
        StringDto intext = new StringDto();            /* 暗号復号元文字列              */
        StringDto outtext = new StringDto();           /* 暗号復号後文字列              */
        StringDto outtext64 = new StringDto();         /* base64変換後文字列            */

        if (dbg_encdec_aes == 1) {
            C_DbgMsg("C_EncDec_AES : start[%d]1:encode 2:decode\n", endeFlg);
        }

        if (endeFlg != C_INFLG_ENCODE && endeFlg != C_INFLG_DECODE) {
            /* 暗号復号フラグ不正 */
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : endeFlg[%d]\n", endeFlg);
            }
            return C_Ret_1;
        }

        /* EVP_CIPHER定義ロード */
//        OpenSSL_add_all_ciphers();

        /* ENGINE不要 */
        /* Load all compiled-in ENGINEs */
        /* ENGINE_load_builtin_engines(); */
        /* Register all available ENGINE implementations of ciphers. */
        /* ENGINE_register_all_ciphers(); */

        /* 変換方式 */
        String cipherAlg = "AES-128-CBC";
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            cipher = null;
        } catch (NoSuchPaddingException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            cipher = null;
        }
        /* 暗号アルゴリズム名のEVP_CIPHER構造体を返却する */
        if (cipher == null) {
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : EVP_get_cipherbyname_ERROR[%s]\n",
                        cipherAlg);
            }
            return C_Ret_2;
        }

        /* 領域確保 */
        /* 2022/10/25 MCCM初版 MOD START */
//    intext = malloc(indataLen+1);
//    outtext = malloc(indataLen + ivLen);
//    outtext64 = malloc(indataLen + ivLen);
        intext.len = malloc(indataLen + 1 + 1000);
        outtext.len = malloc(indataLen + ivLen + 1000);
        outtext64.len = malloc(indataLen + ivLen + 1000);
        /* 2022/10/25 MCCM初版 END START */

        /* 変換元文字列 */
        memset(intext, 0x00, indataLen + 1 + 1000);
        memcpy(intext, indata, indataLen);

        memset(outtext, 0x00, indataLen + ivLen + 1000);
        memset(outtext64, 0x00, indataLen + ivLen + 1000);

        if (dbg_encdec_aes == 1) {
            printHex(key);
            printHex(iv);
        }

        /* EVP_CIPHER_CTX作成 */
        SecretKeySpec ctx = null;
        IvParameterSpec ivs = null;
        try {
            ctx = new SecretKeySpec(getKeyByte(key), "AES");
            ivs = new IvParameterSpec(iv.getBytes());
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            cipher = null;
            ivs = null;
        }

        if (ctx == null) {
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : EVP_CIPHER_CTX_new_ERROR%s\n", "");
            }
            /* メモリ解放 */
            freeALL(intext, outtext, outtext64, null);

            return C_Ret_3;
        }

        /* EVP_CIPHER_CTX初期化 */
//        EVP_CIPHER_CTX_init(ctx);

        /**********************/
        /* 暗号処理／復号処理 */
        /**********************/
        if (endeFlg == C_INFLG_ENCODE) {
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : %s start\n", "decode");
            }

            /* 暗号化 */
            try {
                cipher.init(Cipher.ENCRYPT_MODE, ctx, ivs);
            } catch (Exception e) {
                cipher = null;
            }


            if (cipher == null) {
                if (dbg_encdec_aes == 1) {
                    C_DbgMsg("C_EncDec_AES : EVP_EncryptInit_ex_ERROR[%s]\n",
                            "EncryptInit failed");
                }
                /* メモリ解放 */
                freeALL(intext, outtext, outtext64, null);

                return C_Ret_4;
            }

            /* パディング有効 */
//            EVP_CIPHER_CTX_set_padding(ctx, 1);

            /* 暗号化 */
            byte[] encrypted = null;
            try {
                encrypted = cipher.doFinal(intext.arr.getBytes());
            } catch (Exception e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
            if (encrypted == null) {
                if (dbg_encdec_aes == 1) {
                    C_DbgMsg("C_EncDec_AES : EVP_EncryptUpdate_ERROR[%s]\n",
                            "Encrypt failed");
                }
                /* メモリ解放 */
                freeALL(intext, outtext, outtext64, null);

                return C_Ret_5;
            }

//            /* パディング有効(デフォルト)の場合、最終データ暗号化*/
//            if (!EVP_EncryptFinal_ex(ctx, outtext + out1, &out2)) {
//                if (dbg_encdec_aes == 1) {
//                    C_DbgMsg("C_EncDec_AES : EVP_EncryptFinal_ex_ERROR[%s]\n",
//                            "EncryptFinal failed");
//                }
//                /* メモリ解放 */
//                freeALL(intext, outtext, outtext64, null);
//
//                return C_Ret_6;
//            }

            outtext.arr = new String(encrypted);
            outLen = encrypted.length;
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : outtext[%s]\n", outtext);
                C_DbgMsg("C_EncDec_AES : outLen[%d]\n", outLen);
            }

            /* base64変換処理 →base64 */
            String base64_wk = Base64Utils.encode(encrypted);
            strcpy(outtext64, base64_wk);
            outLen64 = strlen(outtext64);

            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : base64_wk[%s]\n", base64_wk);
                C_DbgMsg("C_EncDec_AES : outtext64[%s]\n", outtext64);
                C_DbgMsg("C_EncDec_AES : outLen64[%d]\n", outLen64);
            }

            /* 変換後文字列 */
            strcpy(outdata, outtext64);
            outdataLen.arr = strlen(outdata);

        } else if (endeFlg == C_INFLG_DECODE) {
            /* 復号 */
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_EncDec_AES : %s start\n", "decode");
            }

            /* base64変換処理 base64→ */
            /* 2022/04/06 null文字認識のため引数追加        */
            /* base64_wk = base64decode(intext, indataLen); */
            /* strcpy((char*)outtext64, (char*)base64_wk);  */
            /* outLen64 = strlen((const char*)outtext64);   */
            String base64_wk = Base64Utils.encode(intext.arr.getBytes());
//            memcpy(outtext64, base64_wk, outLen64);
            outtext64.arr = base64_wk;
            outLen64 = strlen(base64_wk);
            if (dbg_encdec_aes == 1) {
                C_DbgMsg("C_DecodeAES : outtext64[%s]\n", outtext64);
                C_DbgMsg("C_DecodeAES : outLen64[%d]\n", outLen64);
            }

            /* 復号 */
            //cipherを初期化します。復号モードを使用する
            try {
                cipher.init(Cipher.DECRYPT_MODE, ctx, ivs);
            } catch (Exception e) {
                cipher = null;
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
            if (cipher == null) {
                if (dbg_encdec_aes == 1) {
                    C_DbgMsg("C_DecodeAES : EVP_DecryptInit_ex_ERROR[%s]\n",
                            "DecryptInit failed");
                }
                /* メモリ解放 */
                freeALL(intext, outtext, outtext64);

                return C_Ret_4;
            }

            /* パディング有効 */
//            EVP_CIPHER_CTX_set_padding(ctx, 1);

            /* 復号 */
            byte[] decrypted = null;
            byte[] content = Base64.getDecoder().decode(intext.arr);
            try {
                decrypted = cipher.doFinal(content);
            } catch (Exception e) {
                decrypted = null;
                log.error(ExceptionUtil.getExceptionMessage(e));
            }
            if (decrypted == null) {
                if (dbg_encdec_aes == 1) {
                    C_DbgMsg("C_DecodeAES : EVP_DecryptUpdate_ERROR[%s]\n",
                            "Decrypt failed");
                }

                /* メモリ解放 */
                freeALL(intext, outtext, outtext64);

                return C_Ret_5;
            }

//            /* パディング有効(デフォルト)の場合、最終データ復号*/
//            if (!EVP_DecryptFinal_ex(ctx, outtext + out1, & out2)){
//                if (dbg_encdec_aes == 1) {
//                    C_DbgMsg("C_DecodeAES : EVP_DecryptFinal_ex_ERROR[%s]\n",
//                            "DecryptFinal failed");
//                }
//
//                /* メモリ解放 */
//                freeALL(intext, outtext, outtext64,
//                        base64_wk);
//
//                return C_Ret_6;
//            }
//            outLen = out1 + out2;
            outtext.arr = new String(decrypted).replaceAll("[\0]", "");
            ;
            outLen = decrypted.length;
            /* 変換後文字列 */
            strcpy(outdata, outtext);
            outdataLen.arr = strlen(outdata);
        }

        if (dbg_encdec_aes == 1) {
            C_DbgMsg("C_EncDec_AES : outdata[%s]\n", outdata);
            C_DbgMsg("C_EncDec_AES : outdataLen[%d]\n", outdataLen);
        }

        /* ENGINE不要 */
        /* ENGINE_cleanup() */
        ;

//        /* EVP終了 */
//        EVP_cleanup();
//        CRYPTO_cleanup_all_ex_data();
//
//        /* EVP_CIPHER_CTX解放 */
//        EVP_CIPHER_CTX_free(ctx);

        /* メモリ解放 */
        freeALL(intext, outtext, outtext64);

        if (dbg_encdec_aes == 1) {
            C_DbgMsg("C_EncDec_AES : %s\n", "end");
        }

        return C_const_OK;
    }
}
