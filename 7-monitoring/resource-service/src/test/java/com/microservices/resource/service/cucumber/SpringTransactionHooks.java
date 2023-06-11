package com.microservices.resource.service.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTransactionHooks implements BeanFactoryAware {

    private BeanFactory beanFactory;
    private TransactionStatus transactionStatus;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Before(value = "@transaction", order = 100)
    public void startTransaction() {
        transactionStatus = getPlatformTransactionManager().getTransaction(new DefaultTransactionDefinition());
    }

    @After(value = "@transaction", order = 100)
    public void rollBackTransaction() {
        getPlatformTransactionManager().rollback(transactionStatus);
    }

    private PlatformTransactionManager getPlatformTransactionManager() {
        return beanFactory.getBean(PlatformTransactionManager.class);
    }
}
