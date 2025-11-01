package net.navjotsingh.wabai.services;

import org.springframework.stereotype.Service;

@Service
public class AutomationServiceImpl implements AutomationService {
    @Override
    public boolean createSession() {
        System.out.println("creating a session");
        return false;
    }

    @Override
    public boolean handleSavingSession() {
        return false;
    }

    @Override
    public boolean sendMessage() {
        return false;
    }
}
