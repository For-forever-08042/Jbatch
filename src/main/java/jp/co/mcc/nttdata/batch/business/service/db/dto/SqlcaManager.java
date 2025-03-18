package jp.co.mcc.nttdata.batch.business.service.db.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SqlcaManager {

    @Resource
    protected BasicMapper basicMapper;

    String defaultName = "WORK";
    ConcurrentHashMap<String, SqlstmDto> sqlstmDtoConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * init
     *
     * @param sqlstmDto
     */
    public void init(SqlstmDto sqlstmDto) {
        SqlstmDto def = getDefault();
        if (def != null) {
            def.close();
        }
        basicMapper.connect(sqlstmDto);
        sqlstmDtoConcurrentHashMap.put(defaultName, sqlstmDto);
    }

    public SqlstmDto getDefault() {
        if (sqlstmDtoConcurrentHashMap.containsKey(defaultName)) {
            return sqlstmDtoConcurrentHashMap.get(defaultName);
        } else {
            return null;
        }
    }

    public SqlstmDto get(String name) {
        if (sqlstmDtoConcurrentHashMap.containsKey(name)) {
            return sqlstmDtoConcurrentHashMap.get(name);
        } else {
            SqlstmDto sqlstmDto = getDefault().cloneSQLca(name);
            sqlstmDtoConcurrentHashMap.put(name, sqlstmDto);
            return sqlstmDto;
        }
    }

    public void close(SqlstmDto sqlca) {
        if (sqlca != null) {
            close(sqlca.name);
        }
    }

    public void close(String name) {
        if (sqlstmDtoConcurrentHashMap.containsKey(name)) {
            sqlstmDtoConcurrentHashMap.get(name).close();
            sqlstmDtoConcurrentHashMap.remove(name);
        } else {
            log.error("!!!!!!!!!!sql instance not found -{} !!!!!!", name);
        }
    }

    public void commit() {
        SqlstmDto def = getDefault();
        if (def != null) {
            def.commit();
        } else {
            log.error("!!!!!!!!!!no default sql instance not found !!!!!!");
        }
    }

    public void commit(String name) {
        get(name).commit();
    }
    public void commitRelease() {
        getDefault().commitRelease();
    }
    public void commitRelease(String name) {
        get(name).commitRelease();
    }

    public void commitRelease(SqlstmDto sqlstmDto) {
        get(sqlstmDto.name).commitRelease();
    }

    public void rollbackRelease(SqlstmDto sqlstmDto) {
        sqlstmDto.rollback();
        sqlstmDto.release();
        close(sqlstmDto);
    }
}
