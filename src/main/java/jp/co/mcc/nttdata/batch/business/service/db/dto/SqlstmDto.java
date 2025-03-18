package jp.co.mcc.nttdata.batch.business.service.db.dto;

import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.IntegerDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.ItemDto;
import jp.co.mcc.nttdata.batch.business.com.c_aplcom1.dto.StringDto;
import jp.co.mcc.nttdata.batch.fw.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_Ora_NOTFOUND;
import static jp.co.mcc.nttdata.batch.business.com.c_aplcom1.C_aplcom1Service.C_const_Ora_OK;

@Slf4j
public class SqlstmDto {
    public StringDto sql;
    public int sqlcode = C_const_Ora_OK;
    public String errorMsg = "";
    public ResultSet resultSet;
    public PreparedStatement preparedStatement;
    public Map<Integer, Object> currentRow;
    public Connection conn;
    public String name = "WORK";
    public StringDto ORASID;
    public StringDto ORAUSR;
    public StringDto PASSWD;
    public int updateCount;


    public SqlstmDto cloneSQLca(String name) {
        SqlstmDto sqlstmDto = new SqlstmDto();
        sqlstmDto.ORASID = ORASID;
        sqlstmDto.ORAUSR = ORAUSR;
        sqlstmDto.PASSWD = PASSWD;
        sqlstmDto.name = name;
//        mapper.connect(sqlstmDto);
        sqlstmDto.conn = conn;
        sqlstmDto.sql = new StringDto();
        sqlstmDto.sql.arr = (sql == null ? "" : sql.arr);
        log.info("create connection  - {}", name);
        return sqlstmDto;
    }

    public void prepareParameter(Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object item = params[i];
            if (item instanceof ItemDto) {
                preparedStatement.setObject(i + 1, ((ItemDto) item).arr);
            } else if (item instanceof StringDto) {
                preparedStatement.setObject(i + 1, ((StringDto) item).arr);
            } else if (item instanceof IntegerDto) {
                preparedStatement.setObject(i + 1, ((IntegerDto) item).arr);
            } else if (item instanceof String) {
                preparedStatement.setString(i + 1, String.valueOf(item));
            } else if (item instanceof Integer) {
                preparedStatement.setInt(i + 1, Integer.valueOf(String.valueOf(item)));
            } else {
                preparedStatement.setObject(i + 1, item);
            }
        }
//        for (Map.Entry<Integer, StatementItem> item : param.entrySet()) {
//            StatementItem paramItem = item.getValue();
//            if (paramItem.getValue() == null) {
//                preparedStatement.setNull(item.getKey(), paramItem.types);
//                continue;
//            }
//            switch (paramItem.getClass().getSimpleName()) {
//                case "String":
//                    preparedStatement.setString(item.getKey(), (String) paramItem.value);
//                    break;
//                case "Integer":
//                    preparedStatement.setInt(item.getKey(), (Integer) paramItem.value);
//                    break;
//                case "Double":
//                    break;
//                case "BigDecimal":
//                    preparedStatement.setBigDecimal(item.getKey(), (BigDecimal) paramItem.value);
//                    break;
//                case "Date":
//                    preparedStatement.setDate(item.getKey(), (Date) paramItem.value);
//                    break;
//                case "Timestamp":
//                    preparedStatement.setTimestamp(item.getKey(), (Timestamp) paramItem.value);
//                    break;
//                default:
//                    preparedStatement.setString(item.getKey(), String.valueOf(paramItem.value));
//                    break;
//                //TODO
//            }

//        }

    }

    public void checkUpdateStatus() {
        try {
            if (sql.arr.toLowerCase(Locale.ROOT).startsWith("update")) {
                int count = preparedStatement.getUpdateCount();
                updateCount = count;
                if (count <= 0) {
                    sqlcode = C_const_Ora_NOTFOUND;
                }
            }
        } catch (SQLException e) {
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public int arrsiz() {
        int count = 0;
        while (true) {
            try {
                if (resultSet == null) break;
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                log.error(ExceptionUtil.getExceptionMessage(e));
                sqlcode = e.getErrorCode();
                errorMsg = e.getMessage();
                break;
            }
            count++;
        }
        return count;
    }

    public void query(Object... params) {
        try {
            //format data
            sqlcode = C_const_Ora_OK;
            if (params != null && params.length > 0) {
                prepareParameter(params);
            }
            printRealSql(this.sql.arr, params);
            boolean hasResultSet = preparedStatement.execute();
            if (hasResultSet) {
                resultSet = preparedStatement.getResultSet();
            } else {
                int rowsAffected = preparedStatement.getUpdateCount();
            }
            log.info("query  resultSet  - {}", name);
            checkUpdateStatus();
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public void open(Object... params) {
        query(params);
    }

//    public void open() {
//        query();
//    }

    public String getDataByIndex(int index) {
//        StringDto data = new StringDto();
//        data.arr = (String);
        return (String) currentRow.get(index);
    }

    public void rollback() {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public void fetch() {
        try {
            currentRow = new HashMap<>();
            if (resultSet == null) {
                return;
            }
            sqlcode = C_const_Ora_OK;
            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                if (metaData.getColumnCount() == 0) {
                    sqlcode = C_const_Ora_NOTFOUND;
                } else {
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        String name = metaData.getColumnName(i);
                        currentRow.put(i, resultSet.getObject(name));
                    }
                }
            } else {
                sqlcode = C_const_Ora_NOTFOUND;
            }
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public void fetchInto(Object... obj) {
        fetch();
        recData(obj);
    }

    public void recData(Object... obj) {
        if (sqlcode == C_const_Ora_NOTFOUND) {
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] instanceof StringDto) {
                    ((StringDto) obj[i]).arr = "";
                } else if (obj[i] instanceof IntegerDto) {
                    ((IntegerDto) obj[i]).arr = 0;
                } else if (obj[i] instanceof ItemDto) {
                    ((ItemDto) obj[i]).arr = "";
                } else {
                    obj[i] = "";
                }
            }
            return;
        }
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] instanceof StringDto) {
                ((StringDto) obj[i]).arr = String.valueOf(currentRow.get(i + 1));
            } else if (obj[i] instanceof IntegerDto) {
                ((IntegerDto) obj[i]).arr = Integer.valueOf(String.valueOf(currentRow.get(i + 1)));
            } else if (obj[i] instanceof ItemDto) {
                ((ItemDto) obj[i]).arr = currentRow.get(i + 1);
            } else {
                obj[i] = currentRow.get(i + 1);
            }
        }
    }

    public void prepare(StringDto sql) {

        this.sql = sql;
        // 起動時システム日付の取得を行うので、システム日付取得用クエリのみ最初に初期化する
        prepare();
    }

    public void prepare() {

        // 起動時システム日付の取得を行うので、システム日付取得用クエリのみ最初に初期化する
        try {
            sqlcode = C_const_Ora_OK;
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            // システム日付取得クエリ
            preparedStatement = conn.prepareStatement(sql.arr);
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public void declare() {
        try {
            prepare();
            // システム日付取得クエリ
            preparedStatement.clearParameters();
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public void restAndExecute(Object... params) {
        prepare();
        declare();
        query(params);
    }

    public void commit() {
        try {
            sqlcode = C_const_Ora_OK;
            conn.commit();
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public void commitRelease() {
        try {
            sqlcode = C_const_Ora_OK;
            conn.commit();
            release();
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    public void curse_close() {
        try {
            log.info("close   - {}", name);
            sqlcode = C_const_Ora_OK;

            if (null != resultSet) {
                resultSet.close();
                resultSet = null;
            }

            if (null != preparedStatement) {
                preparedStatement.close();
                preparedStatement = null;
            }

            if (null != sql) {
//                sql.arr = "";
            }
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    protected void release() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            log.debug(ExceptionUtil.getExceptionMessage(e));
            sqlcode = e.getErrorCode();
            errorMsg = e.getMessage();
        }
    }

    protected void close() {
//        try {
        curse_close();
//            if(conn!=null){
//                conn.close();
//            }
//        } catch (SQLException e) {
//            log.debug(ExceptionUtil.getExceptionMessage(e));
//            sqlcode = e.getErrorCode();
//            errorMsg = e.getMessage();
//        }
    }

    public static void printRealSql(String sql, Object[] params) {
        try {
            if (params == null || params.length == 0) {
                return;
            }

            if (!match(sql, params)) {
                log.debug("The placeholders in the SQL statement do not match the number of parameters. SQL：" + sql);
                return;
            }

            int cols = params.length;
            Object[] values = new Object[cols];
            System.arraycopy(params, 0, values, 0, cols);

            for (int i = 0; i < cols; i++) {
                Object value = values[i];
                if (value instanceof Date) {
                    values[i] = "'" + value + "'";
                } else if (value instanceof String) {
                    values[i] = "'" + value + "'";
                } else if (value instanceof Boolean) {
                    values[i] = (Boolean) value ? 1 : 0;
                } else if (value instanceof ItemDto) {
                    values[i] = "'" + ((ItemDto) value).toString() + "'";
                }
            }

            String statement = String.format(
                    sql.replaceAll("%(?!\\w)", "%%").replaceAll("\\?", "%s"), values);

            log.debug("The SQL is------------>\n" + statement);
        } catch (Exception e) {
            log.error(ExceptionUtil.getExceptionMessage(e));
        }

    }

    /**
     * ? 和参数的实际个数是否匹配
     *
     * @param sql    SQL 语句，可以带有 ? 的占位符
     * @param params 插入到 SQL 中的参数，可单个可多个可不填
     * @return true 表示为 ? 和参数的实际个数匹配
     */
    private static boolean match(String sql, Object[] params) {
        if (params == null || params.length == 0) return true; // 没有参数，完整输出

        Matcher m = Pattern.compile("(\\?)").matcher(sql);
        int count = 0;
        while (m.find()) {
            count++;
        }

        return count == params.length;
    }
}
