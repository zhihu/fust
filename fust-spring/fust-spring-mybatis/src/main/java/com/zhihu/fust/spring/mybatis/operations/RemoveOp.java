package com.zhihu.fust.spring.mybatis.operations;

import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import com.zhihu.fust.commons.lang.tuple.Pair;
import com.zhihu.fust.spring.mybatis.TableMeta;
import com.zhihu.fust.spring.mybatis.Constants;

public class RemoveOp extends AbstractOperation {

    @Override
    public MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta tableMeta) {
        String condition = Constants.GEN_ENTITY_EQ_SQL_PAIR.apply(
                Pair.of(tableMeta.getKeyColumn(), tableMeta.getKeyProperty()));
        String sql = String.format(SqlOperation.REMOVE.getSql(), tableMeta.getTableName(), condition);

        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addMappedStatement(
                mapperClass, SqlOperation.REMOVE.getMethod(), sqlSource,
                SqlCommandType.DELETE, null, null,
                Integer.class, new NoKeyGenerator(),
                null, null);
    }

}
