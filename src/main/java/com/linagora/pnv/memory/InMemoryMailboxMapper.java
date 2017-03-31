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
package com.linagora.pnv.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.common.base.Objects;

import com.linagora.pnv.Mailbox;
import com.linagora.pnv.MailboxACL;
import com.linagora.pnv.MailboxException;
import com.linagora.pnv.MailboxId;
import com.linagora.pnv.MailboxMapper;
import com.linagora.pnv.MailboxNotFoundException;
import com.linagora.pnv.MailboxPath;
import com.linagora.pnv.Mapper;

public class InMemoryMailboxMapper implements MailboxMapper {
    
    private static final int INITIAL_SIZE = 128;
    private final Map<InMemoryId, Mailbox> mailboxesById;
    private final AtomicLong mailboxIdGenerator = new AtomicLong();

    public InMemoryMailboxMapper() {
        mailboxesById = new ConcurrentHashMap<>(INITIAL_SIZE);
    }

    public void delete(Mailbox mailbox) throws MailboxException {
        mailboxesById.remove(mailbox.getMailboxId());
    }

    public void deleteAll() throws MailboxException {
        mailboxesById.clear();
    }

    public synchronized Mailbox findMailboxByPath(MailboxPath path) throws MailboxException {
        return mailboxesById.values()
        			.stream()
        			.filter(mailbox -> mailbox.generateAssociatedPath().equals(path))
        			.findFirst()
        			.orElseThrow(() -> new MailboxNotFoundException(path));
    }

    public synchronized Mailbox findMailboxById(MailboxId id) throws MailboxException {
    	return Optional.ofNullable(mailboxesById.get(id))
    			.orElseThrow(() -> new MailboxNotFoundException(id.serialize()));
    }

    public List<Mailbox> findMailboxWithPathLike(MailboxPath path) throws MailboxException {
        return mailboxesById.values()
        		.stream()
        		.filter(mailbox -> mailboxMatchesRegex(mailbox, path, path.getName().replace("%", ".*")))
        		.collect(Collectors.toList());
    }

    private boolean mailboxMatchesRegex(Mailbox mailbox, MailboxPath path, String regex) {
        return Objects.equal(mailbox.getNamespace(), path.getNamespace())
            && Objects.equal(mailbox.getUser(), path.getUser())
            && mailbox.getName().matches(regex);
    }

    public MailboxId save(Mailbox mailbox) throws MailboxException {
        InMemoryId id = (InMemoryId) mailbox.getMailboxId();
        if (id == null) {
            id = InMemoryId.of(mailboxIdGenerator.incrementAndGet());
            mailbox.setMailboxId(id);
        }
        mailboxesById.put(id, mailbox);
        return mailbox.getMailboxId();
    }

    public void endRequest() {
    }

    public boolean hasChildren(Mailbox mailboxParameter, char delimiter) throws MailboxException {
    	return mailboxesById.values()
    			.stream()
    			.anyMatch(mailbox -> belongsToSameUser(mailboxParameter, mailbox) 
    							  && mailbox.getName().startsWith(mailboxParameter.getName() + delimiter));		
    }

    private boolean belongsToSameUser(Mailbox mailbox, Mailbox otherMailbox) {
        return Objects.equal(mailbox.getNamespace(), otherMailbox.getNamespace())
            && Objects.equal(mailbox.getUser(), otherMailbox.getUser());
    }

    public List<Mailbox> list() throws MailboxException {
        return new ArrayList<>(mailboxesById.values());
    }

    public <T> T execute(Mapper.Transaction<T> transaction) throws MailboxException {
        return transaction.run();
    }

    @Override
    public void updateACL(Mailbox mailbox, MailboxACL.MailboxACLCommand mailboxACLCommand) throws MailboxException{
        mailbox.setACL(mailbox.getACL().apply(mailboxACLCommand));
    }
}
