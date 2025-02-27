package com.zhihu.fust.spring.mybatis.extend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

/**
 * pass a List<Long> ids to the parameter, you can use @ids in the sql
 * <p>
 * eg: @LangDriver(CollectionDriver.class) @Select("SELECT FROM " + TABLE_NAME + " WHERE id in @ids") int batchDelete(List<Long> ids);
 */
public class CollectionDriver extends XMLLanguageDriver implements LanguageDriver {
    private static final Pattern inPattern = Pattern.compile("@(\\w+)");
    private static final String FOR_EACH = "(<foreach collection=\"$1\" item=\"__item\" "
            + "separator=\",\" >#{__item}</foreach>)";

    @Override
    public SqlSource createSqlSource(Configuration configuration,
                                     String script, Class<?> parameterType) {
        Matcher matcher = inPattern.matcher(script);
        if (matcher.find()) {
            script = matcher.replaceAll(FOR_EACH);
        }

        script = "<script>" + script + "</script>";
        return super.createSqlSource(configuration, script, parameterType);
    }

}
