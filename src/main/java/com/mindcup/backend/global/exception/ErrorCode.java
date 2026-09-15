package com.mindcup.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "exception.common.badrequest"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "exception.common.unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "exception.common.forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "exception.common.notfound"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "exception.common.servererror"),

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "exception.auth.invalidtoken"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "exception.auth.expiredtoken"),
    EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "exception.auth.emailduplicate"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "exception.auth.invalidcredentials"),
    GOOGLE_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "exception.auth.googlefailed"),

    // Mind Cup
    CUP_NOT_FOUND(HttpStatus.NOT_FOUND, "exception.cup.notfound"),
    CUP_ALREADY_CHECKED(HttpStatus.BAD_REQUEST, "exception.cup.alreadychecked"),
    ACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "exception.action.notfound"),
    ACTION_DUPLICATE(HttpStatus.BAD_REQUEST, "exception.action.duplicate"),
    ACTION_FORBIDDEN(HttpStatus.FORBIDDEN, "exception.action.forbidden"),
    FISH_NOT_FOUND(HttpStatus.NOT_FOUND, "exception.fish.notfound"),
    ITEM_NOT_ACQUIRED(HttpStatus.BAD_REQUEST, "exception.world.itemnotacquired"),
    POUR_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "exception.comfort.pourexceeded");

    private final HttpStatus status;
    private final String messageKey;

    ErrorCode(HttpStatus status, String messageKey) {
        this.status = status;
        this.messageKey = messageKey;
    }
}
