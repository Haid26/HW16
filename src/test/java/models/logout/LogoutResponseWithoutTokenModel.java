package models.logout;

import java.util.List;

public record LogoutResponseWithoutTokenModel(List<String> refresh) {
}
