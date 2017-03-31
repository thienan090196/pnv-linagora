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

import static com.linagora.pnv.memory.ListMessageAssert.assertMessages;
import static com.linagora.pnv.memory.ListMessagePropertiesAssert.assertProperties;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.util.SharedByteArrayInputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.linagora.pnv.FlagsUpdateCalculator;
import com.linagora.pnv.Mailbox;
import com.linagora.pnv.MailboxException;
import com.linagora.pnv.MailboxMapper;
import com.linagora.pnv.MailboxMessage;
import com.linagora.pnv.MailboxPath;
import com.linagora.pnv.MailboxSession;
import com.linagora.pnv.MessageId;
import com.linagora.pnv.MessageManager;
import com.linagora.pnv.MessageMapper;
import com.linagora.pnv.MessageMetaData;
import com.linagora.pnv.MessageRange;
import com.linagora.pnv.MessageUid;
import com.linagora.pnv.PropertyBuilder;
import com.linagora.pnv.SimpleMailboxMessage;
import com.linagora.pnv.StandardNames;
import com.linagora.pnv.UpdatedFlags;
import com.linagora.pnv.memory.utils.FlagsBuilder;

public class MessageMapperTest {

    private final static char DELIMITER = '.';
    private static final int LIMIT = 10;
    private static final int BODY_START = 16;
    public static final int UID_VALIDITY = 42;
    public static final String USER_FLAG = "userFlag";

    public static final String CUSTOMS_USER_FLAGS_VALUE = "CustomsFlags";

    private MessageMapper messageMapper;
    private MailboxMapper mailboxMapper;

    private SimpleMailbox benwaInboxMailbox;
    private SimpleMailbox benwaWorkMailbox;
    
    private MailboxMessage message1;
    private MailboxMessage message2;
    private MailboxMessage message3;
    private MailboxMessage message4;
    private MailboxMessage message5;
    private MailboxMessage message6;

    private final Random random = new Random();

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public final void setProducer() throws MailboxException {

        this.messageMapper = new InMemoryMessageMapper(new MockMailboxSession(""), new InMemoryUidProvider(), new InMemoryModSeqProvider());
        this.mailboxMapper = new InMemoryMailboxMapper();

        initData();
    }

    private void initData() throws MailboxException {
        benwaInboxMailbox = createMailbox(new MailboxPath("#private", "benwa", "INBOX"));
        benwaWorkMailbox = createMailbox( new MailboxPath("#private", "benwa", "INBOX"+DELIMITER+"work"));

        message1 = createMessage(benwaInboxMailbox, generateMessageId(), "Subject: Test1 \n\nBody1\n.\n", BODY_START, new PropertyBuilder());
        message2 = createMessage(benwaInboxMailbox, generateMessageId(), "Subject: Test2 \n\nBody2\n.\n", BODY_START, new PropertyBuilder());
        message3 = createMessage(benwaInboxMailbox, generateMessageId(), "Subject: Test3 \n\nBody3\n.\n", BODY_START, new PropertyBuilder());
        message4 = createMessage(benwaInboxMailbox, generateMessageId(), "Subject: Test4 \n\nBody4\n.\n", BODY_START, new PropertyBuilder());
        message5 = createMessage(benwaInboxMailbox, generateMessageId(), "Subject: Test5 \n\nBody5\n.\n", BODY_START, new PropertyBuilder());
        message6 = createMessage(benwaWorkMailbox, generateMessageId(), "Subject: Test6 \n\nBody6\n.\n", BODY_START, new PropertyBuilder());
    }

    @Test
    public void emptyMailboxShouldHaveZeroMessageCount() throws MailboxException {
        assertThat(messageMapper.countMessagesInMailbox(benwaInboxMailbox)).isEqualTo(0);
    }

    @Test
    public void mailboxContainingMessagesShouldHaveTheGoodMessageCount() throws MailboxException {
        saveMessages();
        assertThat(messageMapper.countMessagesInMailbox(benwaInboxMailbox)).isEqualTo(5);
    }

    @Test
    public void mailboxCountShouldBeDecrementedAfterAMessageDelete() throws MailboxException {
        saveMessages();
        messageMapper.delete(benwaInboxMailbox, message1);
        assertThat(messageMapper.countMessagesInMailbox(benwaInboxMailbox)).isEqualTo(4);
    }

    @Test
    public void emptyMailboxShouldNotHaveUnseenMessages() throws MailboxException {
        assertThat(messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox)).isEqualTo(0);
    }

    @Test
    public void mailboxContainingMessagesShouldHaveTheGoodUnseenMessageCount() throws MailboxException {
        saveMessages();
        assertThat(messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox)).isEqualTo(5);
    }

    @Test
    public void mailboxUnSeenCountShouldBeDecrementedAfterAMessageIsMarkedSeen() throws MailboxException {
        saveMessages();
        FlagsUpdateCalculator newFlags = new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE);

        messageMapper.updateFlags(benwaInboxMailbox, newFlags, message1.getUid().toRange());

        assertThat(messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox)).isEqualTo(4);
    }

    @Test
    public void mailboxUnSeenCountShouldBeDecrementedAfterAMessageIsMarkedUnSeen() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        assertThat(messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox)).isEqualTo(5);
    }

    @Test
    public void mailboxUnSeenCountShouldBeDecrementedAfterAMessageDelete() throws MailboxException {
        saveMessages();
        messageMapper.delete(benwaInboxMailbox, message1);
        assertThat(messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox)).isEqualTo(4);
    }

    @Test
    public void deletedMessagesShouldBeRemovedFromStorage() throws MailboxException {
        saveMessages();
        messageMapper.delete(benwaInboxMailbox, message1);
        assertThat(messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(message1.getUid()), MessageMapper.FetchType.Metadata, LIMIT)).isEmpty();
    }

    @Test
    public void deletingUnExistingMessageShouldHaveNoSideEffect() throws MailboxException, IOException {
        saveMessages();
        message6.setUid(messageMapper.getLastUid(benwaInboxMailbox).get().next());
        messageMapper.delete(benwaInboxMailbox, message6);

        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);
        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message1, message2, message3, message4, message5);
    }

    @Test
    public void noMessageShouldBeRetrievedInEmptyMailbox() throws MailboxException {
        assertThat(messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(MessageUid.MIN_VALUE), MessageMapper.FetchType.Metadata, LIMIT)).isEmpty();
    }

    @Test
    public void messagesCanBeRetrievedInMailboxWithRangeTypeOne() throws MailboxException, IOException{
        saveMessages();
        MessageMapper.FetchType fetchType = MessageMapper.FetchType.Full;
        int limit =10;
        MessageAssert.assertThat(messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(message1.getUid()), fetchType, limit).next())
            .isEqualToWithoutAttachment(message1, fetchType);
    }

    @Test
    public void messagesCanBeRetrievedInMailboxWithRangeTypeRange() throws MailboxException, IOException{
        saveMessages();
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper
                .findInMailbox(benwaInboxMailbox, MessageRange.range(message1.getUid(), message4.getUid()), MessageMapper.FetchType.Full, LIMIT);

        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message1, message2, message3, message4);
    }

    @Test
    public void messagesCanBeRetrievedInMailboxWithRangeTypeRangeContainingAHole() throws MailboxException, IOException {
        saveMessages();
        messageMapper.delete(benwaInboxMailbox, message3);
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper
            .findInMailbox(benwaInboxMailbox, MessageRange.range(message1.getUid(), message4.getUid()), MessageMapper.FetchType.Full, LIMIT);
        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message1, message2, message4);
    }

    @Test
    public void messagesCanBeRetrievedInMailboxWithRangeTypeFrom() throws MailboxException, IOException {
        saveMessages();
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper
                .findInMailbox(benwaInboxMailbox, MessageRange.from(message3.getUid()), MessageMapper.FetchType.Full, LIMIT);
        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message3, message4, message5);
    }

    @Test
    public void messagesCanBeRetrievedInMailboxWithRangeTypeFromContainingAHole() throws MailboxException, IOException {
        saveMessages();
        messageMapper.delete(benwaInboxMailbox, message4);
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper
                .findInMailbox(benwaInboxMailbox, MessageRange.from(message3.getUid()), MessageMapper.FetchType.Full, LIMIT);
        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message3, message5);
    }

    @Test
    public void messagesCanBeRetrievedInMailboxWithRangeTypeAll() throws MailboxException, IOException {
        saveMessages();
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);
        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message1, message2, message3, message4, message5);
    }

    @Test
    public void messagesCanBeRetrievedInMailboxWithRangeTypeAllContainingHole() throws MailboxException, IOException {
        saveMessages();
        messageMapper.delete(benwaInboxMailbox, message1);
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper
                .findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);
        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message2, message3, message4, message5);
    }

    @Test
    public void messagesRetrievedUsingFetchTypeMetadataShouldHaveAtLastMetadataDataLoaded() throws MailboxException, IOException{
        saveMessages();
        MessageMapper.FetchType fetchType = MessageMapper.FetchType.Metadata;
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(message1.getUid()), fetchType, LIMIT);
        MessageAssert.assertThat(retrievedMessageIterator.next()).isEqualTo(message1, fetchType);
        assertThat(retrievedMessageIterator).isEmpty();
    }

    @Test
    public void messagesRetrievedUsingFetchTypeHeaderShouldHaveHeaderDataLoaded() throws MailboxException, IOException{
        saveMessages();
        MessageMapper.FetchType fetchType = MessageMapper.FetchType.Headers;
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(message1.getUid()), fetchType, LIMIT);
        MessageAssert.assertThat(retrievedMessageIterator.next()).isEqualTo(message1, fetchType);
        assertThat(retrievedMessageIterator).isEmpty();
    }

    @Test
    public void messagesRetrievedUsingFetchTypeBodyShouldHaveBodyDataLoaded() throws MailboxException, IOException{
        saveMessages();
        MessageMapper.FetchType fetchType = MessageMapper.FetchType.Body;
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(message1.getUid()), fetchType, LIMIT);
        MessageAssert.assertThat(retrievedMessageIterator.next()).isEqualToWithoutAttachment(message1, fetchType);
        assertThat(retrievedMessageIterator).isEmpty();
    }

    @Test
    public void retrievingMessagesWithALimitShouldLimitTheNumberOfMessages() throws MailboxException {
        int limit = 2;
        saveMessages();
        assertThat(messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, limit)).hasSize(2);
    }

    @Test
    public void findRecentUidsInMailboxShouldReturnEmptyListWhenNoMessagesMarkedAsRecentArePresentInMailbox() throws MailboxException {
        assertThat(messageMapper.findRecentMessageUidsInMailbox(benwaInboxMailbox)).isEmpty();
    }

    @Test
    public void findRecentUidsInMailboxShouldReturnListOfMessagesHoldingFlagsRecent() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message2.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message4.getUid()));
        messageMapper.updateFlags(benwaWorkMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message6.getUid()));
        assertThat(messageMapper.findRecentMessageUidsInMailbox(benwaInboxMailbox)).containsOnly(message2.getUid(), message4.getUid());
    }

    @Test
    public void deleteShouldUpdateRecentWhenNeeded() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message2.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message4.getUid()));
        messageMapper.updateFlags(benwaWorkMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message6.getUid()));

        messageMapper.delete(benwaInboxMailbox, message2);

        assertThat(messageMapper.findRecentMessageUidsInMailbox(benwaInboxMailbox)).containsOnly(message4.getUid());
    }

    @Test
    public void deleteShouldNotUpdateRecentWhenNotNeeded() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message2.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message4.getUid()));
        messageMapper.updateFlags(benwaWorkMailbox, new FlagsUpdateCalculator(new Flags(Flag.RECENT), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message6.getUid()));

        messageMapper.delete(benwaInboxMailbox, message1);

        assertThat(messageMapper.findRecentMessageUidsInMailbox(benwaInboxMailbox)).containsOnly(message2.getUid(), message4.getUid());
    }

    @Test
    public void addShouldUpdateRecentWhenNeeded() throws MailboxException {
        message1.setFlags(new Flags(Flag.RECENT));
        messageMapper.add(benwaInboxMailbox, message1);
        message1.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));

        assertThat(messageMapper.findRecentMessageUidsInMailbox(benwaInboxMailbox)).containsOnly(message1.getUid());
    }

    @Test
    public void addShouldNotUpdateRecentWhenNotNeeded() throws MailboxException {
        messageMapper.add(benwaInboxMailbox, message1);
        message1.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));

        assertThat(messageMapper.findRecentMessageUidsInMailbox(benwaInboxMailbox)).isEmpty();
    }

    @Test
    public void findFirstUnseenMessageUidShouldReturnNullWhenNoUnseenMessagesCanBeFound() throws MailboxException {
        assertThat(messageMapper.findFirstUnseenMessageUid(benwaInboxMailbox)).isNull();
    }

    @Test
    public void findFirstUnseenMessageUidShouldReturnUid1WhenUid1isNotSeen() throws MailboxException {
        saveMessages();
        assertThat(messageMapper.findFirstUnseenMessageUid(benwaInboxMailbox)).isEqualTo(message1.getUid());
    }

    @Test
    public void findFirstUnseenMessageUidShouldReturnUid2WhenUid2isSeen() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message3.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message5.getUid()));
        assertThat(messageMapper.findFirstUnseenMessageUid(benwaInboxMailbox)).isEqualTo(message2.getUid());
    }

    @Test
    public void expungeMarkedForDeletionInMailboxShouldReturnEmptyResultOnEmptyMailbox() throws MailboxException, IOException {
        assertThat(messageMapper.expungeMarkedForDeletionInMailbox(benwaInboxMailbox, MessageRange.all())).isEmpty();
    }

    @Test
    public void expungeMarkedForDeletionInMailboxShouldReturnEmptyResultWhenNoMessageInMailboxIsDeleted() throws MailboxException, IOException {
        saveMessages();
        assertThat(messageMapper.expungeMarkedForDeletionInMailbox(benwaInboxMailbox, MessageRange.all())).isEmpty();
        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);

        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message1, message2, message3, message4, message5);
    }

    @Test
    public void expungeShouldReturnCorrectMetadataWithRangeAll() throws MailboxException, IOException {
        saveMessages();
        MetadataMapAssert.assertThat(markThenPerformExpunge(MessageRange.all()))
            .hasSize(2)
            .containsMetadataForMessages(message1, message4);
    }

    @Test
    public void expungeShouldModifyUnderlyingStorageWithRangeAll() throws MailboxException, IOException {
        saveMessages();
        markThenPerformExpunge(MessageRange.all());

        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);

        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message2, message3, message5);
    }

    @Test
    public void expungeShouldReturnCorrectMetadataWithRangeOne() throws MailboxException, IOException {
        saveMessages();
        MetadataMapAssert.assertThat(markThenPerformExpunge(MessageRange.one(message1.getUid())))
            .hasSize(1)
            .containsMetadataForMessages(message1);
    }

    @Test
    public void expungeShouldModifyUnderlyingStorageWithRangeOne() throws MailboxException, IOException {
        saveMessages();
        markThenPerformExpunge(MessageRange.one(message1.getUid()));

        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);

        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message4, message2, message3, message5);
    }

    @Test
    public void expungeShouldReturnCorrectMetadataWithRangeFrom() throws MailboxException, IOException {
        saveMessages();
        MetadataMapAssert.assertThat(markThenPerformExpunge(MessageRange.from(message3.getUid())))
            .hasSize(1)
            .containsMetadataForMessages(message4);
    }

    @Test
    public void expungeShouldModifyUnderlyingStorageWithRangeFrom() throws MailboxException, IOException {
        saveMessages();
        markThenPerformExpunge(MessageRange.from(message3.getUid()));

        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);

        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message1, message2, message3, message5);
    }

    @Test
    public void expungeShouldReturnCorrectMetadataWithRange() throws MailboxException, IOException {
        saveMessages();
        MetadataMapAssert.assertThat(markThenPerformExpunge(MessageRange.range(message3.getUid(), message5.getUid())))
            .hasSize(1)
            .containsMetadataForMessages(message4);
    }

    @Test
    public void expungeShouldModifyUnderlyingStorageWithRange() throws MailboxException, IOException {
        saveMessages();
        markThenPerformExpunge(MessageRange.range(message3.getUid(), message5.getUid()));

        Iterator<MailboxMessage> retrievedMessageIterator = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.all(), MessageMapper.FetchType.Full, LIMIT);

        assertMessages(Lists.newArrayList(retrievedMessageIterator)).containOnly(message1, message2, message3, message5);
    }

    @Test
    public void getHighestMoseqShouldBeEqualToZeroOnEmptyMailbox() throws MailboxException {
        assertThat(messageMapper.getHighestModSeq(benwaInboxMailbox)).isEqualTo(0);
    }

    @Test
    public void insertingAMessageShouldIncrementModSeq() throws MailboxException {
        messageMapper.add(benwaInboxMailbox, message1);
        long modSeq = messageMapper.getHighestModSeq(benwaInboxMailbox);
        assertThat(modSeq).isGreaterThan(0);
        messageMapper.add(benwaInboxMailbox, message2);
        assertThat(messageMapper.getHighestModSeq(benwaInboxMailbox)).isGreaterThan(modSeq);
    }

    @Test
    public void getLastUidShouldReturnEmptyOnEmptyMailbox() throws MailboxException {
        assertThat(messageMapper.getLastUid(benwaInboxMailbox)).isEqualTo(Optional.absent());
    }

    @Test
    public void insertingAMessageShouldIncrementLastUid() throws MailboxException {
        messageMapper.add(benwaInboxMailbox, message1);
        Optional<MessageUid> uid = messageMapper.getLastUid(benwaInboxMailbox);
        assertThat(uid).isNotEqualTo(Optional.absent());
        messageMapper.add(benwaInboxMailbox, message2);
        assertThat(messageMapper.getLastUid(benwaInboxMailbox).get()).isGreaterThan(uid.get());
    }

    @Test
    public void copyShouldIncrementUid() throws MailboxException, IOException {
        saveMessages();
        MessageUid uid = messageMapper.getLastUid(benwaInboxMailbox).get();
        messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(messageMapper.getLastUid(benwaInboxMailbox).get()).isGreaterThan(uid);
    }

    @Test
    public void copyShouldIncrementMessageCount() throws MailboxException, IOException {
        saveMessages();
        messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(messageMapper.countMessagesInMailbox(benwaInboxMailbox)).isEqualTo(6);
    }

    @Test
    public void copyOfUnSeenMessageShouldIncrementUnSeenMessageCount() throws MailboxException, IOException {
        saveMessages();
        messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox)).isEqualTo(6);
    }

    @Test
    public void copyShouldIncrementModSeq() throws MailboxException, IOException {
        saveMessages();
        long modSeq = messageMapper.getHighestModSeq(benwaInboxMailbox);
        messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(messageMapper.getHighestModSeq(benwaInboxMailbox)).isGreaterThan(modSeq);
    }

    @Test
    public void copyShouldCreateAMessageInDestination() throws MailboxException, IOException {
        saveMessages();
        MailboxMessage message7 = SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6);
        messageMapper.copy(benwaInboxMailbox, message7);
        message7.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));
        assertThat(messageMapper.getLastUid(benwaInboxMailbox).get()).isGreaterThan(message6.getUid());

        MailboxMessage result = messageMapper.findInMailbox(benwaInboxMailbox,
            MessageRange.one(messageMapper.getLastUid(benwaInboxMailbox).get()),
            MessageMapper.FetchType.Full,
            LIMIT)
            .next();

        MessageAssert.assertThat(result).isEqualToWithoutUidAndAttachment(message7, MessageMapper.FetchType.Full);
        assertThat(result.getUid()).isEqualTo(messageMapper.getLastUid(benwaInboxMailbox).get());
    }

    @Test
    public void copyOfSeenMessageShouldNotIncrementUnSeenMessageCount() throws MailboxException {
        message6.setFlags(new Flags(Flag.SEEN));
        saveMessages();
        long expectedUnseenMessages = messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox);

        messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(messageMapper.countUnseenMessagesInMailbox(benwaInboxMailbox)).isEqualTo(expectedUnseenMessages);
    }

    @Test
    public void copiedMessageShouldBeMarkedAsRecent() throws MailboxException {
        saveMessages();
        MessageMetaData metaData = messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(
            messageMapper.findInMailbox(benwaInboxMailbox,
                MessageRange.one(metaData.getUid()),
                MessageMapper.FetchType.Metadata,
                LIMIT
            ).next()
            .isRecent()
        ).isTrue();
    }

    @Test
    public void copiedRecentMessageShouldBeMarkedAsRecent() throws MailboxException {
        saveMessages();
        message6.setFlags(new Flags(Flag.RECENT));
        MessageMetaData metaData = messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(
            messageMapper.findInMailbox(benwaInboxMailbox,
                MessageRange.one(metaData.getUid()),
                MessageMapper.FetchType.Metadata,
                LIMIT
            ).next()
                .isRecent()
        ).isTrue();
    }

    @Test
    public void copiedMessageShouldNotChangeTheFlagsOnOriginalMessage() throws MailboxException {
        saveMessages();
        messageMapper.copy(benwaInboxMailbox, SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message6));
        assertThat(
            messageMapper.findInMailbox(benwaWorkMailbox,
                MessageRange.one(message6.getUid()),
                MessageMapper.FetchType.Metadata,
                LIMIT
            ).next()
            .isRecent()
        ).isFalse();
    }

    @Test
    public void flagsReplacementShouldReplaceStoredMessageFlags() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.FLAGGED), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        MessageAssert.assertThat(retrieveMessageFromStorage(message1)).hasFlags(new Flags(Flag.FLAGGED));
    }

    @Test
    public void flagsReplacementShouldReturnAnUpdatedFlagHighlightingTheReplacement() throws MailboxException {
        saveMessages();
        long modSeq = messageMapper.getHighestModSeq(benwaInboxMailbox);
        Iterator<UpdatedFlags> updatedFlags = messageMapper.updateFlags(benwaInboxMailbox,
                new FlagsUpdateCalculator(new Flags(Flag.FLAGGED), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        assertThat(Lists.newArrayList(updatedFlags))
            .containsOnly(UpdatedFlags.builder()
                .uid(message1.getUid())
                .modSeq(modSeq + 1)
                .oldFlags(new Flags())
                .newFlags(new Flags(Flag.FLAGGED))
                .build());
    }

    @Test
    public void flagsAdditionShouldReturnAnUpdatedFlagHighlightingTheAddition() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.FLAGGED), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        long modSeq = messageMapper.getHighestModSeq(benwaInboxMailbox);
        assertThat(messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.ADD), MessageRange.one(message1.getUid())))
            .containsOnly(UpdatedFlags.builder()
                    .uid(message1.getUid())
                    .modSeq(modSeq + 1)
                    .oldFlags(new Flags(Flag.FLAGGED))
                    .newFlags(new FlagsBuilder().add(Flag.SEEN, Flag.FLAGGED).build())
                    .build());
    }

    @Test
    public void flagsAdditionShouldUpdateStoredMessageFlags() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.FLAGGED), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.ADD), MessageRange.one(message1.getUid()));
        MessageAssert.assertThat(retrieveMessageFromStorage(message1)).hasFlags(new FlagsBuilder().add(Flag.SEEN, Flag.FLAGGED).build());
    }

    @Test
    public void flagsRemovalShouldReturnAnUpdatedFlagHighlightingTheRemoval() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new FlagsBuilder().add(Flag.FLAGGED, Flag.SEEN).build(), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        long modSeq = messageMapper.getHighestModSeq(benwaInboxMailbox);
        assertThat(messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REMOVE), MessageRange.one(message1.getUid())))
            .containsOnly(
                UpdatedFlags.builder()
                    .uid(message1.getUid())
                    .modSeq(modSeq + 1)
                    .oldFlags(new FlagsBuilder().add(Flag.SEEN, Flag.FLAGGED).build())
                    .newFlags(new Flags(Flag.FLAGGED))
                    .build());
    }

    @Test
    public void flagsRemovalShouldUpdateStoredMessageFlags() throws MailboxException {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new FlagsBuilder().add(Flag.FLAGGED, Flag.SEEN).build(), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REMOVE), MessageRange.one(message1.getUid()));
        MessageAssert.assertThat(retrieveMessageFromStorage(message1)).hasFlags(new Flags(Flag.FLAGGED));
    }

    @Test
    public void updateFlagsOnRangeShouldAffectMessagesContainedInThisRange() throws MailboxException {
        saveMessages();
        assertThat(messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.range(message1.getUid(), message3.getUid())))
            .hasSize(3);
    }

    @Test
    public void updateFlagsWithRangeFromShouldAffectMessagesContainedInThisRange() throws MailboxException {
        saveMessages();
        assertThat(messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.from(message3.getUid()))).hasSize(3);
    }

    @Test
    public void updateFlagsWithRangeAllRangeShouldAffectAllMessages() throws MailboxException {
        saveMessages();
        assertThat(messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.all())).hasSize(5);
    }

    @Test
    public void messagePropertiesShouldBeStored() throws Exception {
        PropertyBuilder propBuilder = new PropertyBuilder();
        propBuilder.setMediaType("text");
        propBuilder.setSubType("html");
        propBuilder.setTextualLineCount(2L);
        propBuilder.setProperty(StandardNames.NAMESPACE_RFC_2045, StandardNames.MIME_CONTENT_TRANSFER_ENCODING_NAME, "7bit");
        propBuilder.setProperty(StandardNames.MIME_CONTENT_TYPE_PARAMETER_SPACE, StandardNames.MIME_CONTENT_TYPE_PARAMETER_CHARSET_NAME, "US-ASCII");

        MailboxMessage messageWithProperties = createMessage(benwaWorkMailbox, generateMessageId(), "Subject: messagePropertiesShouldBeStored \n\nBody\n.\n", BODY_START, propBuilder);
        MessageMetaData messageMetaData = messageMapper.add(benwaInboxMailbox, messageWithProperties);
        MailboxMessage message = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(messageMetaData.getUid()), MessageMapper.FetchType.Body, 1).next();

        assertProperties(message.getProperties()).containsOnly(propBuilder.toProperties());
    }

    @Test
    public void messagePropertiesShouldBeStoredWhenDuplicateEntries() throws Exception {
        PropertyBuilder propBuilder = new PropertyBuilder();
        propBuilder.setProperty(StandardNames.MIME_CONTENT_LANGUAGE_SPACE, StandardNames.MIME_CONTENT_LANGUAGE_NAME, "us");
        propBuilder.setProperty(StandardNames.MIME_CONTENT_LANGUAGE_SPACE, StandardNames.MIME_CONTENT_LANGUAGE_NAME, "fr");

        MailboxMessage messageWithProperties = createMessage(benwaWorkMailbox, generateMessageId(), "Subject: messagePropertiesShouldBeStoredWhenDuplicateEntries \n\nBody\n.\n", BODY_START, propBuilder);
        MessageMetaData messageMetaData = messageMapper.add(benwaInboxMailbox, messageWithProperties);
        MailboxMessage message = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(messageMetaData.getUid()), MessageMapper.FetchType.Body, 1).next();

        assertProperties(message.getProperties()).containsOnly(propBuilder.toProperties());
    }

    @Test
    public void messagePropertiesShouldBeStoredWhenNoProperty() throws Exception {
        MailboxMessage messageWithProperties = createMessage(benwaWorkMailbox, generateMessageId(), "Subject: messagePropertiesShouldBeStoredWhenNoProperty \n\nBody\n.\n", BODY_START, new PropertyBuilder());
        MessageMetaData messageMetaData = messageMapper.add(benwaInboxMailbox, messageWithProperties);
        MailboxMessage message = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(messageMetaData.getUid()), MessageMapper.FetchType.Body, 1).next();
        assertThat(message.getProperties()).isEmpty();
    }

    @Test
    public void textualLineCountShouldBeWellStored() throws Exception {
        long textualLineCount = 48L;
        PropertyBuilder propBuilder = new PropertyBuilder();
        propBuilder.setTextualLineCount(textualLineCount);

        MailboxMessage messageWithProperties = createMessage(benwaWorkMailbox, generateMessageId(), "Subject: messagePropertiesShouldBeStoredWhenDuplicateEntries \n\nBody\n.\n", BODY_START, propBuilder);
        MessageMetaData messageMetaData = messageMapper.add(benwaInboxMailbox, messageWithProperties);
        MailboxMessage message = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(messageMetaData.getUid()), MessageMapper.FetchType.Body, 1).next();
        assertThat(message.getTextualLineCount()).isEqualTo(textualLineCount);
    }

    public MessageId generateMessageId() {
        return InMemoryMessageId.of(random.nextInt());
    }

    @Test
    public void mediaTypeShouldBeWellStored() throws Exception {
        String mediaType = "plain";
        PropertyBuilder propBuilder = new PropertyBuilder();
        propBuilder.setMediaType(mediaType);

        MailboxMessage messageWithProperties = createMessage(benwaWorkMailbox, generateMessageId(), "Subject: messagePropertiesShouldBeStoredWhenDuplicateEntries \n\nBody\n.\n", BODY_START, propBuilder);
        MessageMetaData messageMetaData = messageMapper.add(benwaInboxMailbox, messageWithProperties);
        MailboxMessage message = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(messageMetaData.getUid()), MessageMapper.FetchType.Body, 1).next();
        assertThat(message.getMediaType()).isEqualTo(mediaType);
    }

    @Test
    public void subTypeShouldBeWellStored() throws Exception {
        String subType = "text";
        PropertyBuilder propBuilder = new PropertyBuilder();
        propBuilder.setSubType(subType);

        MailboxMessage messageWithProperties = createMessage(benwaWorkMailbox, generateMessageId(), "Subject: messagePropertiesShouldBeStoredWhenDuplicateEntries \n\nBody\n.\n", BODY_START, propBuilder);
        MessageMetaData messageMetaData = messageMapper.add(benwaInboxMailbox, messageWithProperties);
        MailboxMessage message = messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(messageMetaData.getUid()), MessageMapper.FetchType.Body, 1).next();
        assertThat(message.getSubType()).isEqualTo(subType);
    }

    @Test
    public void userFlagsShouldBeSupported() throws Exception {
        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(USER_FLAG), MessageManager.FlagsUpdateMode.ADD), MessageRange.one(message1.getUid()));
        MessageAssert.assertThat(retrieveMessageFromStorage(message1)).hasFlags(new Flags(USER_FLAG));
    }

    @Test
    public void userFlagsUpdateShouldReturnCorrectUpdatedFlags() throws Exception {
        saveMessages();
        long modSeq = messageMapper.getHighestModSeq(benwaInboxMailbox);
        assertThat(messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(USER_FLAG), MessageManager.FlagsUpdateMode.ADD), MessageRange.one(message1.getUid())))
            .containsOnly(
                UpdatedFlags.builder()
                    .uid(message1.getUid())
                    .modSeq(modSeq + 1)
                    .oldFlags(new Flags())
                    .newFlags(new Flags(USER_FLAG))
                    .build());
    }

    @Test
    public void messagesShouldBeSavedWithTheirUserFlags() throws Exception {
        MailboxMessage message = SimpleMailboxMessage.copy(benwaInboxMailbox.getMailboxId(), message1);
        messageMapper.add(benwaInboxMailbox, message);
        MessageAssert.assertThat(retrieveMessageFromStorage(message)).hasFlags(new Flags(USER_FLAG));
    }

    @Test
    public void getApplicableFlagShouldUnionAllMessageFlags() throws Exception {
        message1.setFlags(new Flags(Flag.ANSWERED));
        message2.setFlags(new Flags(Flag.DELETED));

        saveMessages();

        assertThat(messageMapper.getApplicableFlag(benwaInboxMailbox))
            .isEqualTo(new FlagsBuilder()
                .add(Flag.ANSWERED, Flag.DELETED)
                .build());
    }

    @Test
    public void getApplicableFlagShouldUnionAllMessageFlagsExceptRecentAndUser() throws Exception {
        message1.setFlags(new Flags(Flag.ANSWERED));
        message2.setFlags(new Flags(Flag.DELETED));

        Flags recentAndUser = new Flags(Flag.RECENT);
        recentAndUser.add(Flag.USER);
        recentAndUser.add(CUSTOMS_USER_FLAGS_VALUE);
        message3.setFlags(recentAndUser);

        saveMessages();

        assertThat(messageMapper.getApplicableFlag(benwaInboxMailbox))
            .isEqualTo(new FlagsBuilder()
                .add(Flag.ANSWERED, Flag.DELETED)
                .add(CUSTOMS_USER_FLAGS_VALUE)
                .build());
    }

    @Test
    public void getApplicableFlagShouldHaveEffectWhenUpdateFlagsByAddingThenComputingApplicableFlagsFromCurrentMailboxState() throws Exception {
        message1.setFlags(new Flags(Flag.ANSWERED));
        message2.setFlags(new Flags(Flag.DELETED));
        FlagsUpdateCalculator newFlags = new FlagsUpdateCalculator(new Flags(Flag.SEEN), MessageManager.FlagsUpdateMode.ADD);

        saveMessages();
        messageMapper.updateFlags(benwaInboxMailbox, newFlags, message1.getUid().toRange());

        assertThat(messageMapper.getApplicableFlag(benwaInboxMailbox))
            .isEqualTo(new FlagsBuilder()
                .add(Flag.ANSWERED, Flag.DELETED, Flag.SEEN)
                .build());
    }

    @Test
    public void getApplicableFlagShouldHaveEffectWhenDeleteMessageThenComputingApplicableFlagsFromCurrentMailboxState() throws Exception {
        message1.setFlags(new Flags(Flag.ANSWERED));
        message2.setFlags(new Flags(Flag.DELETED));

        saveMessages();
        messageMapper.delete(benwaInboxMailbox, message1);

        assertThat(messageMapper.getApplicableFlag(benwaInboxMailbox))
            .isEqualTo(new Flags(Flag.DELETED));
    }

    private Map<MessageUid, MessageMetaData> markThenPerformExpunge(MessageRange range) throws MailboxException {
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.DELETED), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message1.getUid()));
        messageMapper.updateFlags(benwaInboxMailbox, new FlagsUpdateCalculator(new Flags(Flag.DELETED), MessageManager.FlagsUpdateMode.REPLACE), MessageRange.one(message4.getUid()));
        return messageMapper.expungeMarkedForDeletionInMailbox(benwaInboxMailbox, range);
    }

    private SimpleMailbox createMailbox(MailboxPath mailboxPath) throws MailboxException {
        SimpleMailbox mailbox = new SimpleMailbox(mailboxPath, UID_VALIDITY);
        mailbox.setMailboxId(generateId());
        
        mailboxMapper.save(mailbox);

        return mailbox;
    }
    
    private void saveMessages() throws MailboxException {
        messageMapper.add(benwaInboxMailbox, message1);
        message1.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));
        messageMapper.add(benwaInboxMailbox, message2);
        message2.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));
        messageMapper.add(benwaInboxMailbox, message3);
        message3.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));
        messageMapper.add(benwaInboxMailbox, message4);
        message4.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));
        messageMapper.add(benwaInboxMailbox, message5);
        message5.setModSeq(messageMapper.getHighestModSeq(benwaInboxMailbox));
        messageMapper.add(benwaWorkMailbox, message6);
        message6.setModSeq(messageMapper.getHighestModSeq(benwaWorkMailbox));
    }

    private MailboxMessage retrieveMessageFromStorage(MailboxMessage message) throws MailboxException {
        return messageMapper.findInMailbox(benwaInboxMailbox, MessageRange.one(message.getUid()), MessageMapper.FetchType.Metadata, LIMIT).next();
    }
    
    private MailboxMessage createMessage(Mailbox mailbox, MessageId messageId, String content, int bodyStart, PropertyBuilder propertyBuilder) {
        return new SimpleMailboxMessage(messageId, new Date(), content.length(), bodyStart, new SharedByteArrayInputStream(content.getBytes()), new Flags(), propertyBuilder, mailbox.getMailboxId());
    }

    public InMemoryId generateId() {
        return InMemoryId.of(random.nextInt());
    }

}