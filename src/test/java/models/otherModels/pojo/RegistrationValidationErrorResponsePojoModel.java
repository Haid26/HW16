package models.otherModels.pojo;

import java.util.List;

public class RegistrationValidationErrorResponsePojoModel {
    List<String> username;
    List<String> password;

    public List<String> getUsername() {
        return username;
    }

    public List<String> getPassword() {
        return password;
    }
}
