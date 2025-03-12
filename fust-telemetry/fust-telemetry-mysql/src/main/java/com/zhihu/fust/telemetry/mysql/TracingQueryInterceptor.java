package com.zhihu.fust.telemetry.mysql;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.Query;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.jdbc.JdbcConnection;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerSession;
import com.zhihu.fust.telemetry.api.ServiceEntry;
import com.zhihu.fust.telemetry.api.ServiceMeter;
import com.zhihu.fust.telemetry.api.ServiceMeterKind;
import com.zhihu.fust.telemetry.api.Telemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.semconv.SemanticAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A MySQL query interceptor that will report to OpenTelemetry how long each query takes.
 * included in spans.
 */
public final class TracingQueryInterceptor implements QueryInterceptor {
    private static final Logger log = LoggerFactory.getLogger(TracingQueryInterceptor.class);
    private static final String METHOD_OTHERS = "others";
    private static final Set<String> OPERATIONS = new HashSet<>(
            Arrays.asList("insert", "update", "select", "delete"));
    private MysqlConnection connection;
    private boolean interceptingExceptions;
    private final ThreadLocal<SqlQuerySpan> currentSegment = new ThreadLocal<>();
    private static final Telemetry TELEMETRY = Telemetry.create("meter-mysql");

    @Override
    public <T extends Resultset> T preProcess(Supplier<String> sqlSupplier, Query interceptedQuery) {
        try {
            tracing(sqlSupplier.get());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public <T extends Resultset> T postProcess(Supplier<String> sql, Query interceptedQuery,
                                               T originalResultSet, ServerSession serverSession) {
        if (interceptingExceptions && originalResultSet == null) {
            // Error case, the sqlQuerySpan will be finished in TracingExceptionInterceptor.
            return null;
        }

        final String method = getSqlTraceMethod(sql.get());
        if (METHOD_OTHERS.equals(method)) {
            return originalResultSet;
        }

        final SqlQuerySpan sqlQuerySpan = currentSegment.get();
        if (sqlQuerySpan == null) {
            return null;
        }

        try {
            sqlQuerySpan.end();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            currentSegment.remove();
        }
        return originalResultSet;
    }

    private void tracing(String sql) {
        final String method = getSqlTraceMethod(sql);
        if (METHOD_OTHERS.equals(method)) {
            return; // no tracing others
        }
        // strip "jdbc:"
        final URI url = URI.create(safeUrl(connection.getURL().substring(5)));
        final String host = getHost(connection);
        final int port = url.getPort() == -1 ? 3306 : url.getPort();
        final String serviceName = String.format("sql_%s_%d", host, port)
                .replace('.', '-');

        final Span span = TELEMETRY.getTracer()
                .spanBuilder(serviceName)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute(SemanticAttributes.DB_OPERATION, method)
                .setAttribute(SemanticAttributes.DB_CONNECTION_STRING, host + ':' + port)
                .setAttribute(SemanticAttributes.DB_SYSTEM, "MySQL")
                .setAttribute(SemanticAttributes.PEER_SERVICE, serviceName)
                .setAttribute(SemanticAttributes.DB_STATEMENT,
                        sql.length() > 200 ? sql.substring(0, 200) : sql)
                .startSpan();

        // entry method
        final ServiceEntry entry = Telemetry.getServiceEntry();
        final String entryMethod = entry.getEntry();

        // 指标信息
        final ServiceMeter serviceMeter = TELEMETRY.createServiceMeter(ServiceMeterKind.CLIENT);
        serviceMeter.setMethod(entryMethod);
        serviceMeter.setTargetService(serviceName);
        serviceMeter.setTargetMethod(method);
        serviceMeter.setTargetService(serviceName);
        currentSegment.set(new SqlQuerySpan(span, serviceMeter));
    }

    private static String getSqlTraceMethod(String sql) {
        if (sql == null) {
            return "";
        }
        sql = sql.toLowerCase().trim();
        if (sql.length() < 6) {
            return METHOD_OTHERS;
        }
        final String method = sql.substring(0, 6);
        if (!OPERATIONS.contains(method)) {
            return METHOD_OTHERS;
        }
        return method;
    }

    /**
     * replace '_' to '-'
     * URI not support '_'
     */
    private static String safeUrl(String url) {
        return url.replace('_', '-');
    }

    private static String getHost(MysqlConnection connection) {
        if (!(connection instanceof JdbcConnection)) {
            return "unknown";
        }
        return ((JdbcConnection) connection).getHost();
    }

    @Override
    public boolean executeTopLevelOnly() {
        // True means that we don't get notified about queries that other interceptors issue
        return true;
    }

    @Override
    public QueryInterceptor init(MysqlConnection mysqlConnection, Properties properties,
                                 Log log) {
        final TracingQueryInterceptor interceptor = new TracingQueryInterceptor();
        interceptor.connection = mysqlConnection;
        interceptor.interceptingExceptions = false;
        return interceptor;
    }

    @Override
    public void destroy() {
        // Don't care
    }
}
