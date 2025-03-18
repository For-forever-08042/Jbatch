package jp.co.mcc.nttdata.batch.business.com.c_aplcom1;


public class ZipItemDto {
    public long size;
    public String name;
    public String pem;

    @Override
    public String toString() {
        return size + " " + name;
    }
}
