package net.navjotsingh.wabai.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import net.navjotsingh.wabai.model.Birthday;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class AutomationServiceImpl implements AutomationService {
    private final BirthdayManagerService birthdayManagerService;
    private final GenerativeAiService generativeAiService;
    private List<Birthday> birthdays;

    private Playwright playwright;
    private BrowserContext context;
    private Page whatsappPage;
    @Value("${birthday.automation.my-phone-number}")
    private String phoneNumber;
    @Value("${birthday.automation.country}")
    private String country;

    public AutomationServiceImpl(BirthdayManagerService birthdayManagerService, GenerativeAiService generativeAiService) {
        this.birthdayManagerService = birthdayManagerService;
        this.birthdays = birthdayManagerService.getBirthdays();
        this.generativeAiService = generativeAiService;
    }

    @Override
    public void init() {
        List<Birthday> birthdays = findBirthdaysForToday();
        if(birthdays.isEmpty()) {
            System.out.println("No birthday today - " + LocalDate.now());
            return;
        }

        boolean success = createSession();
        if(!success) return;

        boolean sessionReady = verifySavedState();
        if(!sessionReady) return;

        birthdays.forEach(b -> {
            boolean chatWindowOpened = goToChatWindow(b.getContactName());
            if(chatWindowOpened) {
                String birthdayImageUrl = generativeAiService.generateBirthdayCardImage(b.getName());
                String birthdayWish = generativeAiService.generateBirthdayWishMessage(b.getName());

                boolean done = attachImageWithCaptionMessage(b.getContactName(), birthdayImageUrl, birthdayWish);
                if(done) send();
            }
        });

        closeSession();
    }

    @Override
    public List<Birthday> findBirthdaysForToday() {
        return birthdays
                .stream()
                .filter(birthday -> birthday.getBirthDate().getMonth().equals(LocalDate.now().getMonth()))
                .filter(birthday -> birthday.getBirthDate().getDayOfMonth() == LocalDate.now().getDayOfMonth())
                .toList();
    }

    @Override
    public boolean createSession() {
        System.out.println("creating a session");

        try {
            playwright = Playwright.create();
            context = playwright.chromium().launchPersistentContext(
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
            whatsappPage = context.pages().isEmpty()
                    ? context.newPage()
                    : context.pages().get(0);

            // prevent automation detection
            whatsappPage.addInitScript("Object.defineProperty(navigator, 'webdriver', { get: () => false })");

            whatsappPage.navigate("https://web.whatsapp.com/");


        } catch (Exception e) {
            System.out.println("ERROR in createSession(): " + e.getMessage());
            return false;
        }

        return true;
    }


    @Override
    public boolean verifySavedState() {
        // check if first time login / state expired
        try {
            Locator loginBtn = whatsappPage.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Log in with phone number"));

            loginBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(3000));

            System.out.println("First-time login detected. Entering login flow...");

            loginBtn.click();

            whatsappPage.waitForSelector("input[aria-label='Type your phone number.']");
            whatsappPage.locator("input[aria-label='Type your phone number.']").fill(phoneNumber);
            whatsappPage.locator("button:has(span[data-icon='chevron'])").click();

            whatsappPage.locator("div[contenteditable='true'][role='textbox']").fill(country);
            whatsappPage.locator("div[role='listbox'] button:has-text('" + country + "')").click();
            whatsappPage.locator("button:has-text('Next')").click();

            String linkingCode = whatsappPage.getAttribute("[data-link-code]", "data-link-code");
            System.out.println("\nLINKING CODE:");
            System.out.println("------------------------");
            System.out.println(linkingCode);
            System.out.println("------------------------\n");

            // i have 60s to fill the code on whatsapp
            // if the state is unexpired, wait until search box is visible on the homescreen
            whatsappPage.waitForSelector("div[aria-label='Search input textbox'][role='textbox']",
                    new Page.WaitForSelectorOptions().setTimeout(60000));

            System.out.println("Logged in (session persisted).");

        } catch (Exception ignored) {
            // login button never appeared → session is already logged in
            System.out.println("Already logged in, skipping login.");
        }

        return true;
    }

    @Override
    public boolean goToChatWindow(String recipient) {
        try {
            whatsappPage.getByRole(AriaRole.PARAGRAPH).click();
            whatsappPage.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search input textbox"))
                    .fill(recipient);
            whatsappPage.locator("span[title='" + recipient + "']").first().click();
        } catch (Exception ignored) {
            // unable to open the chat window
            System.out.println("ERROR while opening the chat window.");
            return false;
        }
        return true;
    }

    @Override
    public boolean writeMessage(String recipient, String message) {
        try {
            whatsappPage.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Type to " + recipient))
                    .getByRole(AriaRole.PARAGRAPH).click();
            whatsappPage.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Type to " + recipient))
                    .fill(message);
        } catch (Exception ignored) {
            // unable to write the message
            System.out.println("ERROR while writing the message.");
            return false;
        }

        return true;
    }

    @Override
    public boolean attachImage(String recipient, String imagePath) {
        try {
            // click the attach button
            whatsappPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Attach")).click();

            // upload image (input[type=file])
            whatsappPage.waitForSelector(
                    "img[src^='blob:'], div[data-testid='media-preview']",
                    new Page.WaitForSelectorOptions().setTimeout(15000)
            );

            return true;
        } catch (Exception e) {
            System.out.println("ERROR while sending image: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean attachImageWithCaptionMessage(String recipient, String imagePath, String caption) {
        try {
            // click the attach button
            whatsappPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Attach")).click();

            // upload image (input[type=file])
            whatsappPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Photos & videos"))
                    .locator("input[type='file']")
                    .setInputFiles(Paths.get(imagePath));

            whatsappPage.waitForSelector(
                    "img[src^='blob:'], div[data-testid='media-preview']",
                    new Page.WaitForSelectorOptions().setTimeout(15000)
            );

            System.out.println("Image preview detected");

            // type the caption
            if (caption != null && !caption.isBlank()) {
                Locator captionBox = whatsappPage.locator(
                        "div[contenteditable='true'][role='textbox'][aria-label='Type a message']"
                );
                captionBox.click();
                captionBox.pressSequentially(caption, new Locator.PressSequentiallyOptions().setDelay(40)); // more natural typing
            }

            return true;
        } catch (Exception e) {
            System.out.println("ERROR while attaching image and caption: " + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean send() {
        try {
            whatsappPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send"))
                    .click();

            whatsappPage.waitForTimeout(1000);  // wait a second until message is finished sending
            System.out.println("Message sent");
        } catch (Exception ignored) {
            // unable to send the message
            System.out.println("ERROR while sending the message.");
            return false;
        }

        return true;
    }

    @Override
    public void closeSession() {
        try {
            if (context != null) context.close();
            if (playwright != null) playwright.close();
            System.out.println("Session closed.");
        } catch (Exception e) {
            System.out.println("ERROR while closing session.");
        }
    }
}
