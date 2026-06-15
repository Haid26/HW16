package models.otherModels.lombok;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationValidationErrorResponseLombokModel {
    List<String> username;
    List<String> password;


}
