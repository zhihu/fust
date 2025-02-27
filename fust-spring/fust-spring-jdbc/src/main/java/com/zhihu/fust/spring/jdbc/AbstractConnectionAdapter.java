package com.zhihu.fust.spring.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractConnectionAdapter extends AbstractUnsupportedOperationConnection {
    private static final Logger log = LoggerFactory.getLogger(AbstractConnectionAdapter.class);

    /**
     * 实际的 SQL 连接
     */
    protected Connection delegate = null;

    protected boolean autoCommit = true;

    private boolean readOnly = true;

    private boolean closed;

    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;

    @Override
    public final boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    @Override
    public final void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
        recordMethodInvocation(Connection.class, "setAutoCommit", new Class[]{boolean.class},
                new Object[]{autoCommit});
        if (delegate != null) {
            delegate.setAutoCommit(autoCommit);
        }
        log.trace("setAutoCommit this|{} autoCommit|{}", this, autoCommit);
    }

    protected List<SQLException> closeConnection() {
        closed = true;
        List<SQLException> exceptions = new LinkedList<>();
        try {
            if (delegate != null) {
                delegate.close();
            }
        } catch (final SQLException ex) {
            exceptions.add(ex);
        }
        log.trace("close this|{} delegate|{} autoCommit|{} exceptions|{}",
                this, delegate, autoCommit, exceptions);
        return exceptions;
    }

    @Override
    public final boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public final boolean isReadOnly() throws SQLException {
        return readOnly;
    }

    @Override
    public final void setReadOnly(final boolean readOnly) throws SQLException {

        this.readOnly = readOnly;
        recordMethodInvocation(Connection.class, "setReadOnly", new Class[]{boolean.class},
                new Object[]{readOnly});
        if (delegate != null) {
            delegate.setReadOnly(readOnly);
        }
        log.trace("setReadOnly this|{} currentConnection|{} readOnly|{}",
                this, delegate, readOnly);
    }

    @Override
    public final int getTransactionIsolation() throws SQLException {
        return transactionIsolation;
    }

    @Override
    public final void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        recordMethodInvocation(Connection.class, "setTransactionIsolation",
                new Class[]{int.class}, new Object[]{level});

        if (delegate != null) {
            delegate.setTransactionIsolation(level);
        }
        log.trace("setTransactionIsolation this|{} currentConnection|{} transactionIsolation|{}",
                this, delegate, transactionIsolation);
    }

    // ------- Consist with MySQL driver implementation -------

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public final int getHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    @Override
    public final void setHoldability(final int holdability) throws SQLException {
    }
}
