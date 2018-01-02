package com.aalmeida.attachments.uploader.ui;

import com.aalmeida.attachments.uploader.Constants;
import com.aalmeida.attachments.uploader.events.EventBus;
import com.aalmeida.attachments.uploader.logging.Loggable;
import com.aalmeida.attachments.uploader.model.Invoice;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

@Component
public class NotificationTrayIcon extends TrayIcon implements Loggable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationTrayIcon.class.getClass());

    private static final String IMAGE_PATH = "/ui/images/icon.png";
    private static final String TOOLTIP = String.format("%s - %s", Constants.Info.APP_NAME, Constants.Info.APP_VERSION);
    private static final String MESSAGE_TITLE = "Invoice uploaded";
    private static final Image IMAGE = createImage(IMAGE_PATH);

    @Autowired
    private EventBus eventBus;

    private SystemTray tray;
    private TrayIcon trayIcon;

    public NotificationTrayIcon() {
        super(IMAGE, TOOLTIP);
        trayIcon = new TrayIcon(IMAGE, TOOLTIP);
        tray = SystemTray.getSystemTray();
    }

    @PostConstruct
    private void setup() throws AWTException {
        if (!SystemTray.isSupported()) {
            return;
        }
        trayIcon.setToolTip(TOOLTIP);
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);

        eventBus.toObservable().subscribe(object -> {
            if (object instanceof Invoice) {
                trayIcon.displayMessage(MESSAGE_TITLE, getMessageText((Invoice) object), MessageType.INFO);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        tray.remove(trayIcon);
    }

    private static String getMessageText(Invoice invoice) {
        return String.format("Type: %s, Received date: %s", invoice.getEmailFilter().getType(),
                invoice.getReceivedDate());
    }

    private static Image createImage(String path){
        URL imageURL = NotificationTrayIcon.class.getResource(path);
        if (imageURL == null) {
            LOGGER.error("Failed Creating Image. Resource not found: {}", path);
            return null;
        } else {
            return new ImageIcon(imageURL).getImage();
        }
    }
}
