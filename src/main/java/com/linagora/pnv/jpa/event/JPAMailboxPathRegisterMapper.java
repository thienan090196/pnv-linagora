package com.linagora.pnv.jpa.event;

import java.util.Set;

import com.linagora.pnv.event.DistantMailboxPathRegisterMapper;
import com.linagora.pnv.event.MailboxPath;
import com.linagora.pnv.event.Topic;

public class JPAMailboxPathRegisterMapper implements DistantMailboxPathRegisterMapper {

    private final int timeOutInSeconds;

    public JPAMailboxPathRegisterMapper(int timeOutInSeconds) {
        this.timeOutInSeconds = timeOutInSeconds;
    }

    @Override
    public Set<Topic> getTopics(MailboxPath mailboxPath) {
        /*
        Return the list of topics for this mailboxPath
         */
        return null;
    }

    @Override
    public void doRegister(MailboxPath mailboxPath, Topic topic) {
        /*
        Add a topic to the mailboxPath.

        Result will be returned for timeOutInSeconds
         */
    }

    @Override
    public void doUnRegister(MailboxPath mailboxPath, Topic topic) {
        /*
        Remove the registration
         */
    }
}
