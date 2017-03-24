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
import com.linagora.pnv.MaxQuotaManager;
import com.linagora.pnv.QuotaRoot;


public class JPAPerUserMaxQuotaManager implements MaxQuotaManager {

    private final EntityManager entityManager;

    public JPAPerUserMaxQuotaManager(EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    public void setMaxStorage(QuotaRoot quotaRoot, long maxStorageQuota) throws MailboxException {
        /*
        Question 5

        Persist a new MaxUserStorage for this quotaRoot.
        Wrap it into a transaction.
         */
        throw new NotImplementedException();
    }

    @Override
    public void setMaxMessage(QuotaRoot quotaRoot, long maxMessageCount) throws MailboxException {
        /*
        Question 6

        Persist a new MaxUserMessageCount for this quotaRoot.
        Wrap it into a transaction.
         */
        throw new NotImplementedException();
    }

    @Override
    public void setDefaultMaxStorage(long defaultMaxStorage) throws MailboxException {
        /*
        Question 7

        Persist a new MaxDefaultStorage.
        Wrap it into a transaction.
         */
        throw new NotImplementedException();
    }

    @Override
    public void setDefaultMaxMessage(long defaultMaxMessageCount) throws MailboxException {
        /*
        Question 8

        Persist a new MaxDefaultMessageCount.
        Wrap it into a transaction.
         */
        throw new NotImplementedException();
    }

    @Override
    public long getDefaultMaxStorage() throws MailboxException {
        /*
        Question 1 :

         Retrieve the stored MaxDefaultStorage. You can use its DEFAULT_KEY.
         Provide a default value if absent.
         */
        throw new NotImplementedException();
    }

    @Override
    public long getDefaultMaxMessage() throws MailboxException {
        /*
        Question 2 :

         Retrieve the stored MaxDefaultMessageCount. You can use its DEFAULT_KEY.
         Provide a default value if absent.
         */
        throw new NotImplementedException();
    }

    @Override
    public long getMaxStorage(QuotaRoot quotaRoot) throws MailboxException {
        /*
        Question 3 :

         Retrieve the stored MaxUserStorage for this quotaRoot.
         Provide a default value if absent.
         */
        throw new NotImplementedException();
    }

    @Override
    public long getMaxMessage(QuotaRoot quotaRoot) throws MailboxException {
        /*
        Question 4 :

         Retrieve the stored MaxUserMessageCount for this quotaRoot.
         Provide a default value if absent.
         */
        throw new NotImplementedException();
    }
}
