package com.zhihu.fust.spring.mybatis.operations;

import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import com.zhihu.fust.spring.mybatis.TableMeta;

/**
 * @author pangzhanbo
 * @date 2021/4/23
 */
public class BatchPatchOp extends AbstractOperation {
    @Override
    public MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta tableMeta) {

        String values = tableMeta.getValuesForBatchPatch();

        String sql = SqlOperation.BATCH_PATCH.format(tableMeta.getTableName(), values);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

        return addMappedStatement(mapperClass, SqlOperation.BATCH_PATCH.getMethod(), sqlSource, SqlCommandType.UPDATE,
                                  modelClass, null, Boolean.class, new NoKeyGenerator(), null, null);
    }
}
