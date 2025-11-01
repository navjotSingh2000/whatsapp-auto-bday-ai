package net.navjotsingh.wabai.service;

import java.io.IOException;

public interface AutomationService {
    boolean createSession();
    boolean handleSavingSession();
    boolean sendMessage();
}
