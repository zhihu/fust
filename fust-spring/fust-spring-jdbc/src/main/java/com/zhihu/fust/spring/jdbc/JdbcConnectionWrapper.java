package com.zhihu.fust.spring.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * jdbc connection
 * 1. DirectMode: get sql connection directly
 * 2. ParentMode: parent connection control the transaction, children connection execution sql
 */
public class JdbcConnectionWrapper extends AbstractConnectionAdapter implements Connection {

    /**
     * 通过子父连接的方式，实现多数据源的事务管理
     */
    static class JdbcConnectionContext {
        JdbcConnectionWrapper parent;
        List<JdbcConnectionWrapper> children = new ArrayList<>();

        JdbcConnectionContext(JdbcConnectionWrapper parent) {
            this.parent = parent;
        }

        void addChild(JdbcConnectionWrapper child) {
            children.add(child);
        }

        void replay(Connection connection) {
            parent.replayMethodsInvocation(connection);
        }
    }

    private static final String TAG_CURRENT = "current";
    private static final Logger log = LoggerFactory.getLogger(JdbcConnectionWrapper.class);

    private final ConnectionStrategy strategy;
    private final boolean directMode;
    private static final ThreadLocal<Map<String, JdbcConnectionContext>> CONTEXT = new ThreadLocal<>();
    private String transactionName;

    public JdbcConnectionWrapper(ConnectionStrategy strategy, boolean directMode) {
        this.strategy = strategy;
        this.directMode = directMode;
        Map<String, JdbcConnectionContext> contextMap = CONTEXT.get();
        if (!directMode) {
            if (contextMap == null) {
                contextMap = new HashMap<>();
            }
            // use a tag as transaction name
            contextMap.put(TAG_CURRENT, new JdbcConnectionContext(this));
            // set parent context
            CONTEXT.set(contextMap);
        } else {
            transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
            log.trace("Attach transaction|{} this|{}", transactionName, this);
            if (contextMap != null) {
                if (contextMap.containsKey(TAG_CURRENT)) {
                    // reset the transaction name
                    JdbcConnectionContext context = contextMap.remove(TAG_CURRENT);
                    context.parent.transactionName = transactionName;
                    contextMap.put(transactionName, context);
                    log.trace("Success set transactionName|{} for parent|{}", transactionName, context.parent);
                }
                // find the child connection
                contextMap.get(transactionName).addChild(this);
            }
        }
        log.trace("new JdbcConnectionWrapper this|{} strategy|{} directMode|{}", this, strategy, directMode);
    }

    @Override
    public final void commit() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        try {
            if (delegate != null) {
                delegate.commit();
            } else if (CONTEXT.get() != null && transactionName != null) {
                // 存在开了事务，但是没有 DAO 执行情况，此时由于没有子连接，transactionName 为 null
                for (JdbcConnectionWrapper child : CONTEXT.get().get(transactionName).children) {
                    child.delegate.commit();
                }
            }
        } catch (final SQLException ex) {
            exceptions.add(ex);
        }
        log.trace("commit this|{} delegate|{} autoCommit|{} exceptions|{}",
                  this, delegate, autoCommit, exceptions);
        throwSQLExceptionIfNecessary(exceptions);
    }

    @Override
    public final void rollback() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        try {
            if (delegate != null) {
                delegate.rollback();
            } else if (CONTEXT.get() != null && transactionName != null) {
                String name = transactionName;
                for (JdbcConnectionWrapper childConnection : CONTEXT.get().get(name).children) {
                    childConnection.delegate.rollback();
                }
            }
        } catch (final SQLException ex) {
            exceptions.add(ex);
        }
        log.trace("rollback this|{} delegate|{} autoCommit|{} exceptions|{}",
                  this, delegate, autoCommit, exceptions);
        throwSQLExceptionIfNecessary(exceptions);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection(sql).prepareStatement(sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        return getConnection(sql).prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return getConnection(sql).prepareStatement(sql, resultSetType, resultSetConcurrency,
                                                   resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return getConnection(sql).prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return getConnection(sql).prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return getConnection(sql).prepareStatement(sql, columnNames);
    }

    private Connection getConnection(String sql) throws SQLException {
        if (delegate == null) {
            //get the real connection
            delegate = strategy.getConnection(sql);
            if (directMode && CONTEXT.get() != null) {
                // use parent connection info
                String name = transactionName;
                CONTEXT.get().get(name).replay(delegate);
            } else {
                replayMethodsInvocation(delegate);
            }
        }
        log.trace("getConnection this|{} cur|{} strategy|{} child|{} sql|{}",
                  this, delegate, strategy, directMode, sql);
        return delegate;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        if (delegate == null) {
            delegate = strategy.getMasterConnection();
        }
        log.trace("getMetaData this|{} cur|{} strategy|{} child|{}", this, delegate, strategy, directMode);
        return delegate.getMetaData();
    }

    @Override
    public void close() throws SQLException {
        String name = transactionName;
        // 存在父连接，关闭动作由父连接操作。
        // 回滚等操作会在父连接代理给子连接，如果子连接已关闭，导致操作无法执行。
        if (name != null && CONTEXT.get().get(name) != null && directMode) {
            return;
        }

        log.trace("close connection this|{} cur|{} strategy|{}", this, delegate, strategy);

        List<SQLException> exceptions = new LinkedList<>();
        if (CONTEXT.get() != null && name != null) {
            for (JdbcConnectionWrapper connection : CONTEXT.get().get(name).children) {
                exceptions.addAll(connection.closeConnection());
            }
        }

        JdbcConnectionHint.clear();

        // 清理父连接的上下文
        if (CONTEXT.get() != null) {
            if (name == null) {
                name = TAG_CURRENT;
            }
            CONTEXT.get().remove(name);
            if (CONTEXT.get().isEmpty()) {
                log.trace("all transactions done!");
                CONTEXT.remove();
            }
        }

        // 始终释放自己，对于直连模式是必须的
        closeConnection();
        throwSQLExceptionIfNecessary(exceptions);
    }
}
