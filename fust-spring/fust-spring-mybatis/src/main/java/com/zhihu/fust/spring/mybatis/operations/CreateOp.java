package com.zhihu.fust.spring.mybatis.operations;

import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import com.zhihu.fust.spring.mybatis.TableMeta;

public class CreateOp extends AbstractOperation {
    @Override
    public MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta tableMeta) {

        String columns = tableMeta.getColumnsForCreate();
        String values = tableMeta.getValuesForCreate();

        KeyGenerator keyGenerator;
        if (tableMeta.isUseManualId()) {
            keyGenerator = new NoKeyGenerator();
        } else {
            keyGenerator = new Jdbc3KeyGenerator();
        }

        String sql = SqlOperation.CREATE.format(tableMeta.getTableName(), columns, values);
        // extend sql with if test

        String keyProperty = tableMeta.getKeyProperty();
        String keyColumn = tableMeta.getKeyColumn();
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addMappedStatement(mapperClass, SqlOperation.CREATE.getMethod(), sqlSource, SqlCommandType.INSERT, modelClass,
                                  null, Boolean.class, keyGenerator, keyProperty, keyColumn);
    }

}
