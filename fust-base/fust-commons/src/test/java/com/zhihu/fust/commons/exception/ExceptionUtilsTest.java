package com.zhihu.fust.commons.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionUtilsTest {

    @Test
    void testGetRootCauseSingleException() {
        Exception exception = new Exception("Root cause");
        assertEquals(exception, ExceptionUtils.getRootCause(exception));
    }

    @Test
    void testGetRootCauseNestedExceptions() {
        Exception rootCause = new Exception("Root cause");
        Exception middleException = new Exception("Middle exception", rootCause);
        Exception topException = new Exception("Top exception", middleException);
        assertEquals(rootCause, ExceptionUtils.getRootCause(topException));
    }

    @Test
    void testGetThrowableListSingleException() {
        Exception exception = new Exception("Single exception");
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(exception);
        assertEquals(1, throwableList.size());
        assertEquals(exception, throwableList.get(0));
    }

    @Test
    void testGetThrowableListNestedExceptions() {
        Exception rootCause = new Exception("Root cause");
        Exception middleException = new Exception("Middle exception", rootCause);
        Exception topException = new Exception("Top exception", middleException);
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(topException);
        assertEquals(3, throwableList.size());
        assertEquals(topException, throwableList.get(0));
        assertEquals(middleException, throwableList.get(1));
        assertEquals(rootCause, throwableList.get(2));
    }
}