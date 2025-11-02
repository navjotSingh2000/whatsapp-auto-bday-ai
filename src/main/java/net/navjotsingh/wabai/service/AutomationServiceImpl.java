package net.navjotsingh.wabai.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import net.navjotsingh.wabai.model.Birthday;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AutomationServiceImpl implements AutomationService {
    private final BirthdayManagerService birthdayManagerService;
    private List<Birthday> birthdays;
    @Value("${PHONE_NUMBER}")
    private String phoneNumber;
    @Value("${COUNTRY}")
    private String country;

    public AutomationServiceImpl(BirthdayManagerService birthdayManagerService) {
        this.birthdayManagerService = birthdayManagerService;
        this.birthdays = birthdayManagerService.getBirthdays();
    }

    @Override
    public void init() {
        int countBirthdays = findBirthdays().size();
        if(countBirthdays > 0) {
            createSession();
        } else {
            System.out.println("No birthday today - " + LocalDate.now());
        }
    }

    @Override
    public List<Birthday> findBirthdays() {
        return birthdays
                .stream()
                .filter(birthday -> birthday.getBirthDate().getMonth().equals(LocalDate.now().getMonth()))
                .filter(birthday -> birthday.getBirthDate().getDayOfMonth() == LocalDate.now().getDayOfMonth())
                .toList();
    }

    @Override
    public boolean createSession() {
        System.out.println("creating a session");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(1500));
            Page page = browser.newPage();
            page.navigate("https://web.whatsapp.com/");
            page.getByText("Log in with phone number").click();

            page.locator("input[aria-label='Type your phone number.']").fill(phoneNumber);
            page.locator("button:has(span[data-icon='chevron'])").click();
            page.locator("div[contenteditable='true'][role='textbox']").fill(country);
            page.locator("div[role='listbox'] button:has-text('" + country + "')").click();
            page.locator("button:has-text('Next')").click();

            String linkingCode = page.getAttribute("[data-link-code]", "data-link-code");
            System.out.println(linkingCode);
        }

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
