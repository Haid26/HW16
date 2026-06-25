package models.updateUser;

public record UpdateUserSuccessfulResponseModel(String username, Integer id,String firstName,String lastName,String email,
                                                String remoteAddr) {
}
