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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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

    /*
    TODO Rewrite this java 8 style with .stream() map filter findFirst
     */
    public synchronized Mailbox findMailboxByPath(MailboxPath path) throws MailboxException {
        Mailbox result = null;
        for (Mailbox mailbox:mailboxesById.values()) {
            MailboxPath mp = new MailboxPath(mailbox.getNamespace(), mailbox.getUser(), mailbox.getName());
            if (mp.equals(path)) {
                result = mailbox;
                break;
            }
        }
        if (result == null) {
            throw new MailboxNotFoundException(path);
        } else {
            return result;
        }
    }

    /*
    TODO rewrite this with java-8 optionals
     */
    public synchronized Mailbox findMailboxById(MailboxId id) throws MailboxException {
        InMemoryId mailboxId = (InMemoryId)id;
        Mailbox result = mailboxesById.get(mailboxId);
        if (result == null) {
            throw new MailboxNotFoundException(mailboxId.serialize());
        } else {
            return result;
        }
    }

    /*
    TODO rewrite this with java-8 streams: filter, map, collect
     */
    public List<Mailbox> findMailboxWithPathLike(MailboxPath path) throws MailboxException {
        final String regex = path.getName().replace("%", ".*");
        List<Mailbox> results = new ArrayList<>();
        for (Mailbox mailbox:mailboxesById.values()) {
            if (mailboxMatchesRegex(mailbox, path, regex)) {
                results.add(mailbox);
            }
        }
        return results;
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

    /**
     * Do nothing
     */
    public void endRequest() {
        // Do nothing
    }

    /*
    TODO rewrite with java 8 stream : filter, map, findAny, isEmpty...
     */
    public boolean hasChildren(Mailbox mailbox, char delimiter) throws MailboxException {
        String mailboxName = mailbox.getName() + delimiter;
        for (Mailbox box:mailboxesById.values()) {
            if (belongsToSameUser(mailbox, box) && box.getName().startsWith(mailboxName)) {
                return true;
            }
        }
        return false;
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
