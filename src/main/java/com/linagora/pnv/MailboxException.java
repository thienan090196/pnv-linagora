package com.linagora.pnv;


/**
 * Mailbox services should throw this exception in case of unsuccessfull
 * operation.
 */
public class MailboxException extends Exception {

    private static final long serialVersionUID = 4612761817238115904L;

    public MailboxException() {
        super();
    }

    public MailboxException(String message) {
        super(message);
    }

    public MailboxException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
