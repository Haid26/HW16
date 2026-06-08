package models.records;


import java.util.List;

public record RegistrationValidationErrorResponseRecordsModel(List<String> username, List<String> password) {
}
