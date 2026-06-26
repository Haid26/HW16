package models.updateUser;

import java.util.List;

public record UpdateUserNullErorResponseModel(List<String> username, Integer id, List <String> firstName, List <String> lastName, List <String> email,
                                              List<String> remoteAddr ) {
}
