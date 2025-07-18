package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.BaseUserResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ChangeNameTest {
    @BeforeAll
    public static void setupSelenoid() {
//        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.22:3000"; // TODO - не забудь сменить на свой IP перед запуском
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @Test
    public void userCanChangeItsNameTest() throws InterruptedException {
        var user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // generate new name for user
        String newName = RandomData.getName() + " A";
        $(Selectors.byClassName("user-info")).click();
//        TODO - тут я добавил задержку потока, так как по каким то причинам в этом месте без данной задержки тест
//         у меня все время падает на CHROME браузере. Иногда падат и с ней, но в разы реже. У меня в моих тестах так же.
//         Я писал об этом в чате. Не знаю почему это происходит. Я что тока не пробовал, ничего не помогает.
        Thread.sleep(1000);
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible).sendKeys(newName);
//        TODO - тут я закомментировал твой локатор кнопки, и использую другой локатор. Если ты ищещь элемент по
//         классу, то в кавычках надо указывать только один класс, а ты указал несколько. btn - это класс,
//         btn-primary - это тоже класс, mt-3 - это тоже класс. Нельзя указывать несколько классов. И вообще тут проще
//         найти элемент по тексту.
//        $(Selectors.byClassName("btn btn-primary mt-3")).click();
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
        assertEquals(alert.getText(), "✅ Name updated successfully!");
        alert.accept();

        // validate on UI
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, " + newName + "!"));


        // validate on API
//        TODO - здесь ты в эндпоинте Endpoint.CUSTOMER_PROFILE неправильно указал модель, которую он
//         возвращает. Точнее даже не так. Этот эндпоинт может принимать как POST запросы, возвращаю модель
//         ChangeNameResponse, как у тебя и указано и это правильно. Но он так же может принимать GET запросы,
//         возвращая модель BaseUserResponse. Но ты реализовал только один вариант, а именно только имплементация для
//         изменения имени, когда он возвращает модель ChangeNameResponse. Но надо еще реализовать этот же эндпоинт, но чтобы он
//         возвращал другую модель, это можно сделать только создав еще один эндпоинт с другим именем. Я бы просто
//         переименовал первый, и второй назвал немного по другому согласно логике. Например так... смотри в коде
//         где прописаны все эндпоинты.
//        var updatedProfile = new ValidatedCrudRequester<BaseUserResponse>(
//                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
//                Endpoint.CUSTOMER_PROFILE,
//                ResponseSpecs.requestReturnsOK())
//                .get(null);

        var updatedProfile = new ValidatedCrudRequester<BaseUserResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.GET_CUSTOMER_PROFILE, // TODO - сменил на нужный новый эндпоинт
                ResponseSpecs.requestReturnsOK())
                .get(null);

        assertEquals(updatedProfile.getName(), newName);
    }

    @Test
    public void userCannotChangeItsNameTest() {
        var user = AdminSteps.createUser();

        Selenide.open("/login");
        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(user.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(user.getPassword());
        $("button").click();

        // generate invalid name for user
        String newName = RandomData.getName();
        $(Selectors.byClassName("user-info")).click();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);
//        TODO - то же самое что и выше
//        $(Selectors.byClassName("btn btn-primary mt-3")).click();
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert alert = switchTo().alert();
//        TODO - сообщение об ошибке может быть 2х видов, а не одного, поэтому нужно проверять оба, я сделал так
//        assertEquals(alert.getText(), "Name must contain two words with letters only");
        String actualAlertMessage = alert.getText();
        String expectedAlertMessage1 = "Name must contain two words with letters only";
        String expectedAlertMessage2 = "❌ Please enter a valid name.";
        if (!(actualAlertMessage.equals(expectedAlertMessage1) || actualAlertMessage.equals(expectedAlertMessage2))) {
            throw new AssertionError(actualAlertMessage + " is not equal to expected messages");
        }
        alert.accept();

        // validate on UI
        $(Selectors.byText("\uD83C\uDFE0 Home")).click();
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // validate on API
        var updatedProfile = new ValidatedCrudRequester<BaseUserResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.GET_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .get(null);

        assertNotEquals(updatedProfile.getName(), newName);
    }
}
