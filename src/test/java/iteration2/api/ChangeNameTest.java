package iteration2.api;

import generators.RandomData;
import iteration1.api.BaseTest;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ChangeNameTest extends BaseTest {

    @Test
    public void userCanChangeItsNameTest() {
        var userRequest = AdminSteps.createUser();

        var request = ChangeNameRequest.builder()
                .name(RandomData.getName())
                .build();
//      TODO - тут пришлось сменить эндпоинт, так как мы создавали новый, вместо того чтобы переименовать существующий.
//       Но лучше переименовать было конечно, чтобы автоматически сменились названия везде в коде, где он испоользовался.
        var response = new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.UPDATE_CUSTOMER_PROFILE,
                ResponseSpecs.requestReturnsOK())
                .update(request);

        softly.assertThat(response.getMessage()).isEqualTo("Profile updated successfully");
        softly.assertThat(response.getCustomer().getName()).isEqualTo(request.getName());
    }
}
