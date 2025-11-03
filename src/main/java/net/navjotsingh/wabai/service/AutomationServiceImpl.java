package net.navjotsingh.wabai.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import net.navjotsingh.wabai.model.Birthday;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
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

            BrowserContext context = playwright.chromium().launchPersistentContext(
                    Paths.get("whatsapp-session"), // persistent folder where session data will be stored
                    new BrowserType.LaunchPersistentContextOptions()
                            .setHeadless(true)
                            .setSlowMo(1500)    // to allow all page contents and transition to finish loading correctly
                            .setArgs(List.of(   // to prevent blocking from whatsapp
                                    "--disable-blink-features=AutomationControlled",
                                    "--disable-infobars",
                                    "--disable-gpu",
                                    "--no-sandbox",
                                    "--disable-dev-shm-usage",
                                    "--window-size=1400,900",
                                    "--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                                            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36"
                            ))
            );

            // reuse existing whatsApp tab if present, otherwise open a new one
            Page page = context.pages().isEmpty()
                    ? context.newPage()
                    : context.pages().get(0);

            // prevent automation detection
            page.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => false })");

            page.navigate("https://web.whatsapp.com/");

            // first time login / state expired
            Locator loginBtn = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Log in with phone number"));

            try {
                loginBtn.waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(3000));

                System.out.println("First-time login detected. Entering login flow...");

                loginBtn.click();

                page.waitForSelector("input[aria-label='Type your phone number.']");
                page.locator("input[aria-label='Type your phone number.']").fill(phoneNumber);
                page.locator("button:has(span[data-icon='chevron'])").click();

                page.locator("div[contenteditable='true'][role='textbox']").fill(country);
                page.locator("div[role='listbox'] button:has-text('" + country + "')").click();
                page.locator("button:has-text('Next')").click();

                String linkingCode = page.getAttribute("[data-link-code]", "data-link-code");
                System.out.println("\nLINKING CODE:");
                System.out.println("------------------------");
                System.out.println(linkingCode);
                System.out.println("------------------------\n");

            } catch (Exception ignored) {
                // login button never appeared → session is already logged in
                System.out.println("Already logged in, skipping login.");
            }

            // i have 60s to fill the code on whatsapp
            // if the state is unexpired, wait until search box is visible on the homescreen
            page.waitForSelector("div[aria-label='Search input textbox'][role='textbox']",
                    new Page.WaitForSelectorOptions().setTimeout(60000));

            System.out.println("Logged in (session persisted).");

            // send message
            String searchFor = "Navjot Singh";
            String message = "test";
            page.getByRole(AriaRole.PARAGRAPH).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search input textbox"))
                    .fill(searchFor);
            page.getByText(searchFor, new Page.GetByTextOptions().setExact(true))
                    .click();

            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Type to " + searchFor))
                    .getByRole(AriaRole.PARAGRAPH).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Type to " + searchFor))
                    .fill(message);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send"))
                    .click();

            page.waitForTimeout(1000);  // wait a second until message is finished sending
            System.out.println("Message sent");

        } catch (Exception e) {
            System.out.println("ERROR in createSession(): " + e.getMessage());
            return false;
        }

        return true;
    }


    @Override
    public boolean handleSavingSession() {
        return true;
    }

    @Override
    public boolean sendMessage() {
        return true;
    }
}
