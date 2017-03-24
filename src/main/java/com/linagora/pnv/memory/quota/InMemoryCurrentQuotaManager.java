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

package com.linagora.pnv.memory.quota;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.linagora.pnv.MailboxException;
import com.linagora.pnv.MailboxListener;
import com.linagora.pnv.QuotaRoot;
import com.linagora.pnv.StoreCurrentQuotaManager;

public class InMemoryCurrentQuotaManager implements StoreCurrentQuotaManager {


    private final LoadingCache<QuotaRoot, Entry> quotaCache;

    public InMemoryCurrentQuotaManager() {
        this.quotaCache = CacheBuilder.newBuilder().build(new CacheLoader<QuotaRoot, Entry>() {
            @Override
            public Entry load(QuotaRoot quotaRoot) throws Exception {
                return new Entry();
            }
        });
    }

    @Override
    public MailboxListener.ListenerType getAssociatedListenerType() {
        return MailboxListener.ListenerType.EACH_NODE;
    }

    @Override
    public void increase(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        checkArguments(count, size);
        doIncrease(quotaRoot, count, size);
    }

    @Override
    public void decrease(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        checkArguments(count, size);
        doIncrease(quotaRoot, -count, -size);
    }

    @Override
    public long getCurrentMessageCount(QuotaRoot quotaRoot) throws MailboxException {
        try {
            return quotaCache.get(quotaRoot).getCount().get();
        } catch (ExecutionException e) {
            throw new MailboxException("Exception caught", e);
        }
    }

    @Override
    public long getCurrentStorage(QuotaRoot quotaRoot) throws MailboxException {
        try {
            return quotaCache.get(quotaRoot).getSize().get();
        } catch (ExecutionException e) {
            throw new MailboxException("Exception caught", e);
        }
    }

    private void doIncrease(QuotaRoot quotaRoot, long count, long size) throws MailboxException {
        try {
            Entry entry = quotaCache.get(quotaRoot);
            entry.getCount().addAndGet(count);
            entry.getSize().addAndGet(size);
        } catch (ExecutionException e) {
            throw new MailboxException("Exception caught", e);
        }

    }

    private void checkArguments(long count, long size) {
        Preconditions.checkArgument(count > 0, "Count should be positive");
        Preconditions.checkArgument(size > 0, "Size should be positive");
    }

    class Entry {
        private final AtomicLong count;
        private final AtomicLong size;

        public Entry() {
            this.count = new AtomicLong(0);
            this.size = new AtomicLong(0);
        }

        public AtomicLong getCount() {
            return count;
        }

        public AtomicLong getSize() {
            return size;
        }
    }
}
