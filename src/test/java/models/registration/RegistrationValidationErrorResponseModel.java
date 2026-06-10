package models.registration;


import java.util.List;

public record RegistrationValidationErrorResponseModel(List<String> username, List<String> password) {
}
