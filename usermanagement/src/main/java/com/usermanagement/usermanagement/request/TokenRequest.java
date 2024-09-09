package com.usermanagement.usermanagement.request;

import java.util.List;

public record TokenRequest(Integer userId, List<Integer> roleId, java.util.UUID sessionId) {
}
