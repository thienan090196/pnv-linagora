/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package com.linagora.pnv.jpa.quota;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.NotImplementedException;

import com.linagora.pnv.MailboxException;
import com.linagora.pnv.MailboxListener;
import com.linagora.pnv.QuotaRoot;
import com.linagora.pnv.StoreCurrentQuotaManager;
import com.linagora.pnv.jpa.quota.model.JpaCurrentQuota;

/*
Question 4.5

Don't forget to clear your table in the tests :

org.apache.james.mailbox.jpa.EntityManagerFactorySupplier
 */
public class JpaCurrentQuotaManager implements StoreCurrentQuotaManager {

    private final EntityManager entityManager;

    public JpaCurrentQuotaManager(EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    public MailboxListener.ListenerType getAssociatedListenerType() {
        return MailboxListener.ListenerType.ONCE;
    }

    @Override
    public long getCurrentMessageCount(QuotaRoot quotaRoot) throws MailboxException {
        /*
        Question 5

        Using retrieveUserQuota return the current message count of the quota root
         */
        throw new NotImplementedException();
    }

    @Override
    public long getCurrentStorage(QuotaRoot quotaRoot) throws MailboxException {
        /*
        Question 6

        Using retrieveUserQuota return the current size of the quota root
         */
        throw new NotImplementedException();
    }

    @Override
    public void increase(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        /*
        Question 7

        Using retrieveUserQuota, get the current quota

        Then create a new one. Add count and size to it.

        Persist it.

        Wrap the entire method into a transaction.
         */
        throw new NotImplementedException();
    }

    @Override
    public void decrease(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        /*
        Question 8

        Using retrieveUserQuota, get the current quota

        Then create a new one. Minus count and size to it.

        Persist it.

        Wrap the entire method into a transaction.
         */
        throw new NotImplementedException();
    }

    private JpaCurrentQuota retrieveUserQuota(QuotaRoot quotaRoot) {
        /*
        Question 5

        Write the code to retrieve the quota associated to the quotaRoot.

        No need to wrap it into a transaction.
         */
        throw new NotImplementedException();
    }
}
