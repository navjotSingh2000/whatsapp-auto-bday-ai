package net.navjotsingh.wabai.services;

public interface AutomationService {
    boolean createSession();
    boolean handleSavingSession();
    boolean sendMessage();
}
