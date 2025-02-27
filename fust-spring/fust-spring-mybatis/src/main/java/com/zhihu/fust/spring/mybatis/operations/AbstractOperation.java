package com.zhihu.fust.spring.mybatis.operations;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhihu.fust.spring.mybatis.TableMeta;
import com.zhihu.fust.spring.mybatis.util.TableMetaUtil;

public abstract class AbstractOperation implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(AbstractOperation.class);
    protected Configuration configuration;
    protected LanguageDriver languageDriver;
    protected MapperBuilderAssistant builderAssistant;

    @Override
    public void bind(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {
        this.builderAssistant = builderAssistant;
        this.configuration = builderAssistant.getConfiguration();
        this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        Class<?> modelClass = TableMetaUtil.getModelClassByMapper(mapperClass);
        if (modelClass != null) {
            TableMeta tableMeta = TableMetaUtil.getTableMetaByModelClass(modelClass);
            inject(mapperClass, modelClass, tableMeta);
        }
    }

    public abstract MappedStatement inject(Class<?> mapperClass, Class<?> modelClass, TableMeta tableMeta);

    /**
     * 查询
     */
    protected MappedStatement addSelectMappedStatement(Class<?> mapperClass, String id, SqlSource sqlSource,
                                                       Class<?> resultType, String resultMap) {
        return addMappedStatement(mapperClass, id, sqlSource, SqlCommandType.SELECT, null, resultMap,
                                  resultType,
                                  new NoKeyGenerator(), null, null);
    }

    /**
     * 添加 MappedStatement 到 Mybatis 容器
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    protected MappedStatement addMappedStatement(Class<?> mapperClass, String id, SqlSource sqlSource,
                                                 SqlCommandType sqlCommandType, Class<?> parameterClass,
                                                 String resultMap, Class<?> resultType,
                                                 KeyGenerator keyGenerator,
                                                 String keyProperty, String keyColumn) {
        String statementName = mapperClass.getName() + "." + id;
        if (configuration.hasStatement(statementName, false)) {
            logger.error("{" + statementName + "} " +
                         "Has been loaded by XML or SqlProvider, ignoring the injection of the SQL.");
            return null;
        }
        /* 缓存逻辑处理 */
        boolean isSelect = false;
        if (sqlCommandType == SqlCommandType.SELECT) {
            isSelect = true;
        }
        return builderAssistant.addMappedStatement(id, sqlSource, StatementType.PREPARED, sqlCommandType, null,
                                                   null, null,
                                                   parameterClass, resultMap, resultType, null, !isSelect,
                                                   isSelect, false, keyGenerator, keyProperty, keyColumn,
                                                   configuration.getDatabaseId(), languageDriver, null);
    }
}
