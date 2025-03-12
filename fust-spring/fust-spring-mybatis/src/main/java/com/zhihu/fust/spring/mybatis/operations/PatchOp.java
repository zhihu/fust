package com.zhihu.fust.spring.mybatis.operations;

import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import com.zhihu.fust.commons.lang.tuple.Pair;
import com.zhihu.fust.spring.mybatis.TableMeta;
import com.zhihu.fust.spring.mybatis.Constants;

/**
 * @author pangzhanbo
 * @date 2021/4/23
 */
public class PatchOp extends AbstractOperation {
    @Override
    public MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta tableMeta) {

        String values = tableMeta.getValuesForPatch();
        String condition = Constants.GEN_EQ_SQL_PAIR.apply(Pair.of(tableMeta.getKeyColumn(), tableMeta.getKeyProperty()));

        String sql = SqlOperation.PATCH.format(tableMeta.getTableName(), values, condition);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

        return addMappedStatement(mapperClass, SqlOperation.PATCH.getMethod(), sqlSource, SqlCommandType.UPDATE,
                                  modelClass, null, Boolean.class, new NoKeyGenerator(), null, null);
    }
}
