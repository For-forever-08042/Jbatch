package jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto;

import jp.co.mcc.nttdata.batch.business.service.db.dto.SqlstmDto;
import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
public class SQLDA {
    public int N;
    //value
    public Object[] V;
    //length
    public int[] L;
    //type
    public int[] T;
    //nullable ?
    public int[] I;
    //columns
    public int F;
    public char S;
    public short M;
    public short C;
    public char X;
    public short Y;
    public short Z;
    public long CP;

    public void init(SqlstmDto sqlca) {
        try {
            ResultSetMetaData metaData = sqlca.resultSet.getMetaData();
            F = metaData.getColumnCount();
            T = new int[F];
            L = new int[F];
            I = new int[F];
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                T[i] = metaData.getColumnType(i + 1);
                L[i] = metaData.getColumnDisplaySize(i + 1);
                I[i] = metaData.isNullable(i + 1);
            }

        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlca.sqlcode = e.getErrorCode();
            sqlca.errorMsg = e.getMessage();
        }
    }

    public void updateData(Map<Integer, Object> currentRow) {
        V = currentRow.values().toArray(new Object[0]);
//        for (int i = 0; i < V.length; i++) {
//            if(V[i]==null){
//                I[i] = -1;
//            }else{
//                I[i] = 1;
//            }
//        }
    }
}
