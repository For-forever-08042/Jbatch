package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;

@Slf4j
public class FileStatusDto {
    public String error;
    public int fd;
    public String fileName;
    public int bufferLineIndex;
    public FileChannel fileChannel;
    FileChannel readerChannel;
    public int readerIndexStart;
    public FileLock lock;
    public int OK = 0;
    public int NG = 1;
    public boolean isFeof = false;
    BufferedReader bufferedReader;
    public Charset charset = Charset.forName("UTF-8");

    public void setReaderChannel(FileChannel readerChannel) {
        this.readerChannel = readerChannel;
    }

    public int feof() {
        return isFeof ? NG : OK;
    }


    public String freadStr(int size, int count) {
        try {
            error = "";
            isFeof = false;
            readerChannel.position(readerIndexStart);
            ByteBuffer[] buffers = new ByteBuffer[count];
            for (int i = 0; i < count; i++) {
                buffers[i] = ByteBuffer.allocate(size);
            }
            long bytesRead = readerChannel.read(buffers);
            readerIndexStart += size * count;
            StringBuilder builder = new StringBuilder();
            if (bytesRead != -1) {

                for (ByteBuffer buffer : buffers) {
                    buffer.flip();
                }

                for (ByteBuffer buffer : buffers) {
                    if (buffer.hasRemaining()) {
                        builder.append(charset.decode(buffer));
                    }
                }
            } else {
                isFeof = true;
            }
            return builder.toString();
        } catch (IOException e) {
            error = e.getMessage();
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        return "";
    }

    public void freadDto(FileReadBaseDto obj, int count) {

        Field[] files = obj.getClass().getDeclaredFields();
        for (int c = 0; c < count; c++) {
            try {
                error = "";
                isFeof = false;
                readerChannel.position(readerIndexStart);
                for (int i = 0; i < files.length; i++) {
                    Field item = files[i];
                    try {
                        Object itemDto = item.get(obj);
                        int len = 0;
                        if (itemDto instanceof ItemDto) {
                            len = ((ItemDto) itemDto).len;
                        } else if (itemDto instanceof IntegerDto) {
                            len = String.valueOf(((IntegerDto) itemDto).arr).length();
                        } else if (itemDto instanceof StringDto) {
                            len = ((StringDto) itemDto).len;
                        }
                        ByteBuffer buffers = ByteBuffer.allocate(len);
                        readerIndexStart += len;
                        long bys = readerChannel.read(buffers);
                        if (bys != -1) {
                            buffers.flip();
                            String result = null;
                            if (buffers.hasRemaining()) {
                                result = charset.decode(buffers).toString();
                            } else {
                                result = "";
                            }
                            if (itemDto instanceof ItemDto) {
                                ((ItemDto) itemDto).arr = result;
                            } else if (itemDto instanceof IntegerDto) {
                                ((IntegerDto) itemDto).arr = StringUtils.isBlank(result) ? 0 : Integer.valueOf(result);
                            } else if (itemDto instanceof StringDto) {
                                ((StringDto) itemDto).arr = result;
                            }
                        } else {
                            isFeof = true;
                        }
                    } catch (IllegalAccessException e) {
                        error = e.getMessage();
                    }
                }
            } catch (Exception e) {
                error = e.getMessage();
                log.error(ExceptionUtil.getExceptionMessage(e));
            }

        }
    }

    public String fget(int length) {
        String resData = "";
        try {
            error = "";
            isFeof = false;
            if (bufferedReader == null) {
                bufferLineIndex = 0;
                bufferedReader = new BufferedReader(new FileReader(fileName, charset));
            }
            resData = bufferedReader.readLine();
            if (resData==null) {
                isFeof = true;
                return null;
            }
            bufferLineIndex++;
        } catch (Exception e) {
            error = e.getMessage();
            log.error(ExceptionUtil.getExceptionMessage(e));
        }
        resData = resData.substring(0, Math.max(Math.min(resData.length(), length), 0));
        return resData + "\n";
    }

    public int flock() {
        try {
            fd = OK;
            error = "";
            fileChannel.position(fileChannel.size() == 0 ? 0 : (fileChannel.size() - 1));
            lock = fileChannel.tryLock();
            return OK;
        } catch (IOException e) {
            error = e.getMessage();
        }
        fd = NG;
        return NG;
    }

    public int close() {
        try {
            error = "";
            if (lock != null) {
                lock.release();
            }
            if (fileChannel != null) {
                fileChannel.close();
            }
            if (readerChannel != null) {
                readerChannel.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
                bufferedReader = null;
            }
            fd = OK;
            return OK;
        } catch (IOException e) {
            error = e.getMessage();
        }
        fd = NG;
        return NG;
    }

    public int write(String data) {
        try {
            error = "";
            fd = OK;
            ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
            return fileChannel.write(buf);
        } catch (IOException e) {
            error = e.getMessage();
        }
        fd = NG;
        return NG;
    }

    public int write(String data, int count) {
        try {
            error = "";
            fd = OK;
            ByteBuffer buf = ByteBuffer.wrap(data.getBytes());
            int ws = 0;
            for (int i = 0; i < count; i++) {
                ws += fileChannel.write(buf);
            }
            return ws;
        } catch (IOException e) {
            error = e.getMessage();
        }
        fd = NG;
        return NG;
    }

    public int fsync() {
        try {
            error = "";
            fd = OK;
            lock.release();
            return OK;
        } catch (IOException e) {
            error = e.getMessage();
        }
        fd = NG;
        return NG;
    }

    public void flush() {
        try {
            error = "";
            fd = OK;
            fileChannel.force(true);
        } catch (IOException e) {
            error = e.getMessage();
            fd = NG;
        }
    }
}
