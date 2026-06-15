package models.common;


import java.util.List;

public record UsernamePasswordValidationErrorResponseModel(List<String> username, List<String> password) {
}
