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
    boolean sendMessage(String recipient, String message);
    void closeSession();
}
