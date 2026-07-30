package com.aidb.assistant.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;

/**
 * Custom annotation to extract the username from the current Authentication context.
 * Using @AuthenticationPrincipal(expression = "username") extracts the principal's username.
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "username")
public @interface CurrentUser {
}
