package models.records;


public record RegistrationResponseRecordsModel(String username, String firstName, String lastName,
                                              String email, String remoteAddr, Integer id) {}
