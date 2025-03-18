package jp.co.mcc.nttdata.batch.fw.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Arrays;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_NG;
import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_OK;

@Slf4j
public class IconvUtil {
    public static int main(Charset from, Charset to, String inFileName, String outFileName) {
        try {
            String originalString = FileUtil.readFile(inFileName, from.name());
            byte[] originalBytes = originalString.getBytes(from);

            ByteBuffer byteBuffer = ByteBuffer.wrap(originalBytes);

            CharsetEncoder encoder = to.newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            CharsetDecoder decoder = from.newDecoder();

            CharBuffer charBuffer = decoder.decode(byteBuffer);

            ByteBuffer outputBytes = encoder.encode(charBuffer);

            int length = 0;
            for (byte item : outputBytes.array()) {
                if (item == 0) {
                    break;
                }
                length++;
            }
            FileUtil.writeFile(outFileName, new String(Arrays.copyOfRange(outputBytes.array(), 0, length), to), to.name());
            return C_const_OK;
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return C_const_NG;
        }
    }

    public static int main(String from, String to, String inFileName, String outFileName) {
        return main(Charset.forName(from), Charset.forName(to), inFileName, outFileName);
    }

    public static String change(String fromC, String toC, String srcStr) {
        try {
            Charset from = Charset.forName(fromC);
            Charset to = Charset.forName(toC);

            CharsetDecoder decoder = from.newDecoder();
            CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(srcStr.getBytes(fromC)));

            CharsetEncoder encoder = to.newEncoder();

            ByteBuffer outputBytes = encoder.encode(charBuffer);
            outputBytes.flip();
            int length = 0;
            for (byte item : outputBytes.array()) {
                if (item == 0) {
                    break;
                }
                length++;
            }
            return new String(Arrays.copyOfRange(outputBytes.array(), 0, length), to);
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return "";
    }


    public static String main(String from, String to, String originalString) {
        try {

            byte[] originalBytes = originalString.getBytes(from);

            ByteBuffer byteBuffer = ByteBuffer.wrap(originalBytes);

            CharsetEncoder encoder = Charset.forName(to).newEncoder();
            CharsetDecoder decoder = Charset.forName(from).newDecoder();

            CharBuffer charBuffer = decoder.decode(byteBuffer);

            ByteBuffer outputBytes = encoder.encode(charBuffer);

            charBuffer.flip();
          int length = 0;
          for (byte item : outputBytes.array()) {
            if (item == 0) {
              break;
            }
            length++;
          }
            return new String(Arrays.copyOfRange(outputBytes.array(), 0, length), to);
        } catch (CharacterCodingException | UnsupportedEncodingException e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
            return null;
        }
    }
}
