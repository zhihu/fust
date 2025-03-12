package com.zhihu.fust.spring.mybatis.operations;

import static com.zhihu.fust.spring.mybatis.operations.SqlOperation.BATCH_CREATE;

import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import com.zhihu.fust.spring.mybatis.TableMeta;

public class BatchCreateOp extends AbstractOperation {
    @Override
    public MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta tableMeta) {
        String columns = tableMeta.getColumnsForBatchCreate();
        String values = tableMeta.getValuesForBatchCreate();

        KeyGenerator keyGenerator;
        if (tableMeta.isUseManualId()) {
            keyGenerator = new NoKeyGenerator();
        } else {
            keyGenerator = new Jdbc3KeyGenerator();
        }
        String sql = BATCH_CREATE.format(tableMeta.getTableName(), columns, values);
        String keyProperty = tableMeta.getKeyProperty();
        String keyColumn = tableMeta.getKeyColumn();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addMappedStatement(mapperClass, BATCH_CREATE.getMethod(), sqlSource, SqlCommandType.INSERT,
                                  modelClass, null, Boolean.class, keyGenerator, keyProperty, keyColumn);
    }
}
