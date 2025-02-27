package com.zhihu.fust.spring.mybatis.operations;

import static com.zhihu.fust.spring.mybatis.operations.SqlOperation.UPDATE;

import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import com.zhihu.fust.commons.lang.tuple.Pair;
import com.zhihu.fust.spring.mybatis.TableMeta;
import com.zhihu.fust.spring.mybatis.Constants;

public class UpdateOp extends AbstractOperation {

    @Override
    public MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta tableMeta) {

        String values = tableMeta.getValuesForUpdate();
        String condition = Constants.GEN_ENTITY_EQ_SQL_PAIR.apply(
                Pair.of(tableMeta.getKeyColumn(), tableMeta.getKeyProperty()));

        String sql = UPDATE.format(tableMeta.getTableName(), values, condition);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

        return addMappedStatement(mapperClass, UPDATE.getMethod(), sqlSource, SqlCommandType.UPDATE, modelClass,
                                  null, Boolean.class, new NoKeyGenerator(), null, null);
    }

}
