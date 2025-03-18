package jp.co.mcc.nttdata.batch.business.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * TestGetFile
 *
 * @author wangjinniu
 * @since 2024-07-16
 */
public class TestOpenssl {

    public static void main(String[] args) throws Exception {
        System.out.println("START=================================================================================");
        String content = "2300017480054";
        String key = "Ikeai83Na93Je7nd";
        String iv = "o3rz5jEJoC8estRa";

        System.out.println("源内容：" + content);

        String encrypted = encrypt(content, key, iv);
        System.out.println("加密后：" + encrypted);

        String decrypted = decrypt(encrypted, key, iv);
        System.out.println("解密后：" + decrypted);
        System.out.println("END=================================================================================");
    }

    /**
     * AES/CBC/PKCS5Padding 加密
     *
     * @param content    待加密的内容
     * @param secret_key 用于生成密钥的 key，自定义即可，加密与解密必须使用同一个，如果不一致，则抛出异常
     * @param vector_key 用于生成算法参数规范的 key，自定义即可，加密与解密必须使用同一个，如果不一致，解密的内容可能会造成与源内容不一致
     *                   <p>
     *                   1、secret_key、vector_key：AES 时必须是 16 个字节，DES 时必须是 8 字节.
     *                   2、secret_key、vector_key 值不建议使用中文，如果是中文，注意一个汉字是3个字节。
     *                   </p>
     * @return 返回 Cipher 加密后的数据，对加密后的字节数组用 Base64 进行编码转成了可视字符串，如 IzLtStoBJqkBU/jRca7D2A==
     * @throws Exception
     */
    public static String encrypt(String content, String secret_key, String vector_key) throws Exception {
        // 返回 Cipher 加密后的数据，对加密后的字节数组用 Base64 进行编码转成了可视字符串，如 7giH2bqIMH3kDMIg8gq0nY
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //使用 SecretKeySpec(byte[] key, String algorithm) 创建密钥. 算法要与 Cipher.getInstance 保持一致.
        SecretKeySpec sks = new SecretKeySpec(secret_key.getBytes(), "AES");
        IvParameterSpec ivs = new IvParameterSpec(vector_key.getBytes());

        // init(int opMode,Key key,AlgorithmParameterSpec params)：初始化 Cipher，
        // 1、Cipher.ENCRYPT_MODE 表示加密模式
        // 2、key 表示加密密钥
        // 3、params 表示算法参数规范，使用 CBC 有向量模式时，必须传入,如果是 ECB-无向量模式,那么可以不传
        cipher.init(Cipher.ENCRYPT_MODE, sks, ivs);

        // byte[] doFinal(byte[] content)：对 content 完成加密操作，如果 cipher.init 初始化时使用的解密模式，则此时是解密操作.
        // 返回的是加密后的字节数组，如果直接 new String(byte[] bytes) 是会乱码的，可以借助 BASE64 转为可视字符串，或者转成 16 进制字符
        byte[] encrypted = cipher.doFinal(content.getBytes());
        String result = Base64.getEncoder().encodeToString(encrypted);

        return result;
    }

    /**
     * AES/CBC/PKCS5Padding 解密
     *
     * @param base64Encode 待解密的内容，因为加密时使用了 Base64 进行了编码，所以这里传入的也是 Base64 编码后的可视化字符串
     * @param secret_key   用于生成密钥的 key，自定义即可，加密与解密必须使用同一个，如果不一致，则抛出异常
     * @param vector_key   用于生成算法参数规范的 key，自定义即可，加密与解密必须使用同一个，如果不一致，解密的内容可能会造成与源内容不一致
     *                     <p>
     *                     1、secret_key、vector_key：AES 时必须是 16 个字节，DES 时必须是 8 字节.
     *                     2、secret_key、vector_key 值不建议使用中文，如果是中文，注意一个汉字是3个字节。
     *                     </p>
     * @return
     * @throws Exception
     */
    public static String decrypt(String base64Encode, String secret_key, String vector_key) throws Exception {
        //实例化 Cipher 对象。加密算法/反馈模式/填充方案，解密与加密需要保持一致.
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        //创建密钥。算法也要与实例化 Cipher 一致.
        SecretKeySpec sks = new SecretKeySpec(secret_key.getBytes(), "AES");
        IvParameterSpec ivs = new IvParameterSpec(vector_key.getBytes());

        //初始化 cipher。使用解密模式.
        cipher.init(Cipher.DECRYPT_MODE, sks, ivs);

        //将 Base64 编码的内容解码成字节数组(因为加密的时候，对密文使用了 Base64编码，所以这里需要先解码)
        byte[] content = Base64.getDecoder().decode(base64Encode);
        //执行解密操作。返回解密后的字节数组，此时可以使用 String(byte bytes[]) 转成源字符串.
        byte[] decrypted = cipher.doFinal(content);

        return new String(decrypted);
    }
}
