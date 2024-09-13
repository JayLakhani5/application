package com.usermanagement.usermanagement.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "user")
@Getter
@Setter
public class UserProperties {
    private String passwordPattern;
    private String contactNumberPattern;
}

