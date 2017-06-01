package com.aalmeida.attachments.uploader.email;

import com.aalmeida.attachments.uploader.Loggable;
import com.aalmeida.utils.FileUtils;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.util.BASE64DecoderStream;
import org.apache.commons.io.IOUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.util.SharedByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class EmailMonitor implements Loggable {

    private final static String PROTOCOL = "imaps";

    private static final int KEEP_ALIVE_FREQUENCY = 1000 * 30;
    //private static final int WAIT_RETRY = 1000 * 30;

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
        folder.addMessageCountListener(new MessageCountListener() {
            @Override
            public void messagesRemoved(final MessageCountEvent pEvent) { }

            @Override
            public void messagesAdded(final MessageCountEvent pEvent) {
                final Message[] messages = pEvent.getMessages();
                if (logger().isTraceEnabled()) {
                    logger().trace("Going to extract data and processing '{}' new received emails.", messages.length);
                }
                for (final Message message : messages) {
                    processEmail(message);
                }
            }
        });

        new Thread(() -> {
            try {
                final Date beginDate = getBeginDate(daysOld);
                if (logger().isDebugEnabled()) {
                    logger().debug("Checking emails on folder '{}' with date >= '{}'.", monitorFolder, beginDate);
                }
                final Message[] messages = folder.search(buildSearchTerms(beginDate, subjectPattern));
                //final MimeMessage[] messages = (MimeMessage[]) folder.search(buildSearchTerms(beginDate, subjectPattern));
                final FetchProfile fetchProfile = new FetchProfile();
                fetchProfile.add(FetchProfile.Item.ENVELOPE);
                folder.fetch(messages, fetchProfile);
                if (logger().isTraceEnabled()) {
                    logger().trace("Going to extract data and processing '{}' emails.", messages.length);
                }
                for (final Message msg : messages) {
                    processEmail(msg);
                }
            } catch (MessagingException e) {
                logger().error("Failed to get emails.", e);
            }
        }).start();

        if (folder instanceof IMAPFolder) {
            final IMAPFolder f = (IMAPFolder) folder;
            final Thread t = new Thread(new KeepAlive(f, KEEP_ALIVE_FREQUENCY), "IdleConnectionKeepAlive");
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
                return emailMatch(message, subjectSearchPattern);
            }
        };
        return new AndTerm(new ReceivedDateTerm(ComparisonTerm.GT, beginDate), subjectMatch);
    }

    private boolean emailMatch(final Message message, final String subjectSearchPattern) {
        try {
            if (message == null || message.getSubject() == null) {
                return false;
            }
            ((IMAPMessage) message).setPeek(true);

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

    private void processEmail(final Message message) {
        if (logger().isTraceEnabled()) {
            logger().trace("Processing email. message={}", message);
        }
        if (message == null) {
            return;
        }
        if (listener != null) {
            ((IMAPMessage) message).setPeek(true);
            final Email email = fetchEmailData(message);
            if (email != null) {
                if (logger().isTraceEnabled()) {
                    logger().trace("Email added to be processed. email={}", email);
                }
                listener.emailReceived(email);
            }
        } else {
            if (logger().isWarnEnabled()) {
                logger().warn("Listener is null.");
            }
        }
    }

    private Email fetchEmailData(final Message message) {
        try {
            final Email email = new Email();
            email.setSubject(message.getSubject());
            if (message.getContent() != null && message.getContent() instanceof MimeMultipart) {
                final MimeMultipart mp = (MimeMultipart) message.getContent();
                email.setAttachments(getAttachments(mp));
            }
            email.setFromAddress(getAddress(message.getFrom()));
            email.setReceivedDate(message.getReceivedDate().getTime());
            if (logger().isTraceEnabled()) {
                logger().trace("Email fetched. email={}", email);
            }
            return email;
        } catch (MessagingException | IOException e) {
            logger().error("Failed to fetch email.", e);
        }
        return null;
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
