package net.navjotsingh.wabai.service;

import com.microsoft.playwright.Page;
import net.navjotsingh.wabai.model.Birthday;

import java.io.IOException;
import java.util.List;

public interface AutomationService {
    void init();
    List<Birthday> findBirthdaysForToday();
    boolean createSession();
    boolean verifySavedState();
    boolean goToChatWindow(String recipient);
    boolean writeMessage(String recipient, String message);
    boolean attachImage(String recipient, String imagePath);
    boolean attachImageWithCaptionMessage(String recipient, String imagePath, String caption);
    boolean send();
    void closeSession();
}
