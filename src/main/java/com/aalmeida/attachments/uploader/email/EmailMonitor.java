package com.aalmeida.attachments.uploader.email;

import com.aalmeida.attachments.uploader.FilterProperties;
import com.aalmeida.attachments.uploader.Loggable;
import com.aalmeida.utils.FileUtils;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.util.BASE64DecoderStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.*;
import javax.mail.util.SharedByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class EmailMonitor implements Loggable {

    private final static String PROTOCOL = "imaps";

    private static final int KEEP_ALIVE_FREQUENCY = 1000 * 30;
    private static final int WAIT_RETRY = 1000 * 30;

    private final String imapHost;
    private final String username;
    private final String password;
    private final String monitorFolder;
    private final String temporaryFolder;
    private final int daysOld;
    private final String subjectPattern;

    private EmailListener listener;
    private Folder folder;

    public EmailMonitor(final String pImapHost, final String pUsername, final String pPassword,
                        final String pMonitorFolder, final String pTemporaryFolder, final int pDaysOld,
                        final String pSubjectPattern) {
        this.imapHost = pImapHost;
        this.username = pUsername;
        this.password = pPassword;
        this.monitorFolder = pMonitorFolder;
        this.temporaryFolder = pTemporaryFolder;
        this.daysOld = pDaysOld;
        this.subjectPattern = pSubjectPattern;
    }

    @PostConstruct
    public void init() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", PROTOCOL);
        //properties.setProperty("mail.debug", Boolean.toString(true));

        final Session session = Session.getInstance(properties, null);

        final Store store = session.getStore(PROTOCOL);
        store.connect(imapHost, username,password);

        if (logger().isTraceEnabled()) {
            logger().trace("Going to get all folders.");
            final Folder[] folderList = store.getDefaultFolder().list();
            printFolders(folderList);
        }

        folder = store.getFolder(monitorFolder);
        folder.open(Folder.READ_WRITE);

        final Date beginDate = getBeginDate(daysOld);

        if (logger().isDebugEnabled()) {
            logger().debug("Checking emails with date >= {}", beginDate);
        }
        final Message messages[] = folder.search(buildSearchTerms(beginDate, subjectPattern));
        //final MimeMessage[] messages = (MimeMessage[]) folder.search(buildSearchTerms(beginDate, subjectPattern));
        if (logger().isTraceEnabled()) {
            logger().trace("Going to process {} emails.", messages.length);
        }
        final FetchProfile fetchProfile = new FetchProfile();
        fetchProfile.add(FetchProfile.Item.ENVELOPE);
        folder.fetch(messages, fetchProfile);
        for (final Message msg : messages) {
            processEmail(msg);
        }
        folder.addMessageCountListener(new MessageCountListener() {
            @Override
            public void messagesRemoved(MessageCountEvent pEvent) { }

            @Override
            public void messagesAdded(MessageCountEvent pEvent) {
                Message[] msgs = pEvent.getMessages();
                if (logger().isTraceEnabled()) {
                    logger().trace("Found {} emails.", msgs.length);
                }
                for (final Message msg : msgs) {
                    processEmail(msg);
                }
            }
        });
        if (folder instanceof IMAPFolder) {
            final IMAPFolder f = (IMAPFolder) folder;
            Thread t = new Thread(new KeepAlive(f, KEEP_ALIVE_FREQUENCY), "IdleConnectionKeepAlive");
            t.start();

            while (!Thread.interrupted()) {
                if (t.isInterrupted()) {
                    throw new Exception("Interrupted keep alive thread.");
                }
                try {
                    f.idle();
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
            // Shutdown keep alive thread
            if (t.isAlive()) {
                t.interrupt();
            }
        } else {
            for (;;) {
                Thread.sleep(KEEP_ALIVE_FREQUENCY);
                folder.getMessageCount();
            }
        }
    }

    public void setListener(final EmailListener listener) {
        this.listener = listener;
    }

    @PreDestroy
    public void destroy() throws MessagingException {
        folder.close(true);
    }

    private SearchTerm buildSearchTerms(final Date beginDate, final String subjectSearchPattern) {
        final SearchTerm subjectMatch = new SearchTerm() {
            public boolean match(Message message) {
                try {
                    if (message == null || message.getSubject() == null) {
                        return false;
                    }
                    if (message.getSubject().matches(subjectSearchPattern)) {
                        if (logger().isTraceEnabled()) {
                            logger().trace("Email subject matches. subject={}, receivedDate={}", message.getSubject(),
                                    message.getReceivedDate());
                        }
                        return true;
                    }
                } catch (MessagingException ex) {
                    logger().error("Failed to search messages.", ex);
                }
                return false;
            }
        };
        return new AndTerm(new SizeTerm(ComparisonTerm.GT, 100),
                new AndTerm(new ReceivedDateTerm(ComparisonTerm.GT, beginDate), subjectMatch));
    }

    private void processEmail(final Message message) {
        try {
            if (message == null) {
                return;
            }
            final Email email = new Email();
            email.setSubject(message.getSubject());
            if (message.getContent() != null && message.getContent() instanceof MimeMultipart) {
                final MimeMultipart mp = (MimeMultipart) message.getContent();
                email.setAttachments(getAttachments(mp));
            }
            email.setFromAddress(getAddress(message.getFrom()));
            email.setReceivedDate(message.getReceivedDate().getTime());

            // do not mark message as read
            message.setFlag(Flags.Flag.SEEN, false);

            if (logger().isTraceEnabled()) {
                logger().trace("Email fetched. email={}", email);
            }
            if (listener != null) {
                listener.emailReceived(email);
            } else {
                if (logger().isWarnEnabled()) {
                    logger().warn("Listener is null");
                }
            }
        } catch (MessagingException | IOException e) {
            logger().error("Failed to process the email.", e);
        }
    }

    private List<File> getAttachments(final MimeMultipart mp) throws IOException, MessagingException {
        List<Attachment> attachments = null;
        for (int part = 0; part < mp.getCount(); part++) {
            BodyPart b = mp.getBodyPart(part);
            Object content = b.getContent();
            if (content instanceof MimeMultipart) {
                getAttachments((MimeMultipart) content);
            } else if ((content instanceof BASE64DecoderStream || content instanceof SharedByteArrayInputStream)) {
                if (attachments == null) {
                    attachments = new ArrayList<>();
                }
                final Attachment attachment = new Attachment(IOUtils.toByteArray(b.getInputStream()), b.getFileName());
                attachments.add(attachment);
                if (logger().isTraceEnabled()) {
                    logger().trace("Attachment added. attachment={}", attachment);
                }
            }
        }

        List<File> filesSaved = null;
        if (attachments != null && !attachments.isEmpty()) {
            // order attachments list
            attachments.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
            // store files
            filesSaved = new ArrayList<>(attachments.size());
            for (Attachment attachment : attachments) {
                filesSaved.add(FileUtils.saveFile(attachment.getContent(), temporaryFolder, attachment.getName(),
                        Integer.toString(filesSaved.size())));
            }
        }
        return filesSaved;
    }

    private String getAddress(final Address[] addresses) {
        if (addresses == null) {
            return null;
        }
        for (final Address addr : addresses) {
            final String email = (addr == null ? null : ((InternetAddress) addr).getAddress());
            if (email != null) {
                return email;
            }
        }
        return null;
    }

    private Date getBeginDate(final int daysOld) {
        Calendar calBegin = Calendar.getInstance();
        calBegin.add(Calendar.DAY_OF_YEAR, -daysOld);
        return calBegin.getTime();
    }

    private void printFolders(final Folder[] folderList) throws MessagingException {
        if (folderList == null) {
            return;
        }
        for (final Folder folder : folderList) {
            logger().trace("Folder name: '{}'", folder.getFullName());
            printFolders(folder.list());
        }
    }

    private static class KeepAlive implements Runnable {

        private IMAPFolder folder;
        private int keepAliveFrequency;

        /**
         * Instantiates a new keep alive.
         *
         * @param pFolder
         *            the folder
         * @param pKeepAliveFrequency
         *            the keep alive frequency
         */
        KeepAlive(IMAPFolder pFolder, int pKeepAliveFrequency) {
            this.folder = pFolder;
            keepAliveFrequency = pKeepAliveFrequency;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(keepAliveFrequency);
                    // Perform a NOOP just to keep alive the connection
                    folder.doCommand(arg0 -> {
                        arg0.simpleCommand("NOOP", null);
                        return null;
                    });
                } catch (InterruptedException | MessagingException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
