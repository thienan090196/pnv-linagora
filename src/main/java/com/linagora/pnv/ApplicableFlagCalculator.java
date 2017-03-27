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

package com.linagora.pnv;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import com.google.common.base.Preconditions;

public class ApplicableFlagCalculator {

    private final Iterable<MailboxMessage> mailboxMessages;

    public ApplicableFlagCalculator(Iterable<MailboxMessage> mailboxMessages) {
        Preconditions.checkNotNull(mailboxMessages);
        this.mailboxMessages = mailboxMessages;
    }

    public Flags computeApplicableFlags() {
        List<Flags> messageFlags = StreamSupport.stream(mailboxMessages.spliterator(), false)
            .map(MailboxMessage::createFlags)
            .collect(Collectors.toList());
        return getFlags(messageFlags);
    }

    private Flags getFlags(List<Flags> messageFlags) {
        Flags flags = new Flags();

        for (Flags flag : messageFlags) {
            flags.add(flag);
        }

        flags.remove(Flag.RECENT);
        flags.remove(Flag.USER);

        return flags;
    }
}
