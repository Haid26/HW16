package models.registration;


public record RegistrationResponseModel(String username, String firstName, String lastName,
                                        String email, String remoteAddr, Integer id) {}
