package com.linagora.pnv.jpa.event;

import java.util.Date;

/*
 * TODO
 *
 * Modify this class to store it this JPA.
 *
 * Use it in jpa.event.JPAMailboxPathRegisterMapper
 */
public class JPARegistration {

    private String mailboxPath;

    private String topic;

    private Date expireDate;

    public JPARegistration(String mailboxPath, String topic, Date expireDate) {
        this.mailboxPath = mailboxPath;
        this.topic = topic;
        this.expireDate = expireDate;
    }

    public JPARegistration() {
    }

    /*
        TODO write equals and hashcode
     */

}
