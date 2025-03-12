package com.zhihu.fust.spring.mybatis.operations;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

import com.zhihu.fust.spring.mybatis.TableMeta;

public class FindOp extends AbstractOperation {
    @Override
    public MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta table) {
        String sql = SqlOperation.FIND.format(table.getTableName(), table.getKeyColumn(), table.getKeyProperty());
        SqlSource sqlSource = new RawSqlSource(configuration, sql, Object.class);
        String resultId = mapperClass.getName() + "." + modelClass.getSimpleName();
        return this.addSelectMappedStatement(mapperClass, SqlOperation.FIND.getMethod(), sqlSource, modelClass, resultId);
    }
}
