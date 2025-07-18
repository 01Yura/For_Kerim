package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            BaseUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            BaseAccountResponse.class
    ),

//    TODO -  закомментил твой вариант и предлагаю создать 2 варианта. Один для изменения имени (с логикой из твоего эндпоинта),
//     второй для получения имени.
//    CUSTOMER_PROFILE(
//            "/customer/profile",
//            ChangeNameRequest.class,
//            ChangeNameResponse.class
//    ),

    UPDATE_CUSTOMER_PROFILE("/customer/profile", ChangeNameRequest.class, ChangeNameResponse.class),
    GET_CUSTOMER_PROFILE("/customer/profile", BaseModel.class, BaseUserResponse.class),

    TRANSFER(
            "/accounts/transfer",
            TransferMoneyRequest.class,
            TransferMoneyResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            BaseAccountResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            BaseAccountResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
