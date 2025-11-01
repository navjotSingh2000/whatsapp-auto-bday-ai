package net.navjotsingh.wabai.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AutomationServiceImpl implements AutomationService {
    private final BirthdayManagerService birthdayManagerService;

    public AutomationServiceImpl(BirthdayManagerService birthdayManagerService) {
        this.birthdayManagerService = birthdayManagerService;
    }

    @Override
    public boolean createSession() {
        System.out.println("creating a session");
        birthdayManagerService.getBirthdays();

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
