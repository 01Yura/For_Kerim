package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.BaseAccountResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.DepositSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DepositMoneyTest {
    @BeforeAll
    public static void setupSelenoid() {
//        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.22:3000";  // TODO - не забудь сменить на свой IP перед запуском
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    public void userCanDepositMoneyTest() {
        // create user
        var user = AdminSteps.createUser();
        // create account for user
        var account = DepositSteps.createAccount(user);

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(text("Welcome, noname!"));

        // UI deposit process
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
//       TODO - Во-первых кнопку по нескольким классам искать нельзя, и тут нет "ul li" тэгов, надо испоользовать
//        другую конструкцию. Твой код закомментирую
//       $(Selectors.byClassName("form-control account-selector")).click();
//       $$("ul li").filter(Condition.visible)
//                .findBy(text(account.getAccountNumber()))
//                .click();
        $(Selectors.byXpath("//option[text()='-- Choose an account --']/ancestor::select")).selectOptionContainingText(account.getAccountNumber());


        int depositAmount = RandomData.getRandom().nextInt(1000);
        $("[placeholder='Enter amount']").setValue(depositAmount + "");
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("✅ Successfully deposited");
        alert.accept();

        // validate on API
        var updatedAccount = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get(null)
                .extract()
                .jsonPath()
                .getList("", BaseAccountResponse.class)
                .stream().filter(a -> a.getId() == account.getId())
                .findFirst().orElseThrow();

        assertThat(updatedAccount.getBalance()).isEqualTo(account.getBalance() + depositAmount);
    }

    @Test
    public void userCannotDepositMoneyTest() {
        // create user
        var user = AdminSteps.createUser();
        // create account for user
        var account = DepositSteps.createAccount(user);

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(text("Welcome, noname!"));

        // UI deposit process
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();
//       TODO - то же самое. Твой код закомментирую
//       $(Selectors.byClassName("form-control account-selector")).click();
//       $$("ul li").filter(Condition.visible)
//                .findBy(text(account.getAccountNumber()))
//                .click();
        $(Selectors.byXpath("//option[text()='-- Choose an account --']/ancestor::select")).selectOptionContainingText(account.getAccountNumber());

        int limitAmount = 5000;
        int depositAmount = RandomData.getRandom().nextInt(5000) + limitAmount;
        $("[placeholder='Enter amount']").setValue(depositAmount + "");
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains("❌ Please deposit less or equal to 5000$.");
        alert.accept();

        // validate on API
        var updatedAccount = new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get(null)
                .extract()
                .jsonPath()
                .getList("", BaseAccountResponse.class)
                .stream().filter(a -> a.getId() == account.getId())
                .findFirst().orElseThrow();

        assertThat(updatedAccount.getBalance()).isEqualTo(account.getBalance());
    }
}
