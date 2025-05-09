package com.example.be12fin5verdosewmthisbe.dbtransaction;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataSourceAspect {

    @Before("@annotation(com.example.be12fin5verdosewmthisbe.dbtransaction.ReadOnlyTransactional)")
    public void setReadDataSource() {
        DataSourceContextHolder.setDataSourceKey("READ");
    }

    @After("@annotation(com.example.be12fin5verdosewmthisbe.dbtransaction.ReadOnlyTransactional)")
    public void clearDataSource() {
        DataSourceContextHolder.clearDataSourceKey();
    }
}