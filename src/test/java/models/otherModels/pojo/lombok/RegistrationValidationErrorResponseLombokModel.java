package models.otherModels.pojo.lombok;

import lombok.Data;

import java.util.List;

@Data
public class RegistrationValidationErrorResponseLombokModel {
    List<String> username;
    List<String> password;


}
