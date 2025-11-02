package net.navjotsingh.wabai.jobs;

import net.navjotsingh.wabai.model.Birthday;
import net.navjotsingh.wabai.service.AutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class BirthdayCheckJob {
    @Autowired
    private final AutomationService automationService;

    public BirthdayCheckJob(AutomationService automationService) {
        this.automationService = automationService;
    }

    @Scheduled(cron = "0 0 9 * * ?")  // run every day at 9 AM
    public void runJob() {
        List<Birthday> list = automationService.findBirthdays();
        System.out.println(list);
        automationService.createSession();
    }
}
