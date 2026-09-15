package com.mindcup.backend.domain.cup.dto;

import com.mindcup.backend.domain.cup.entity.CustomAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomActionResponse {
    private Long actionId;
    private String actionName;
    private String actionType;

    public static CustomActionResponse from(CustomAction action) {
        return new CustomActionResponse(
                action.getActionId(),
                action.getActionName(),
                action.getActionType()
        );
    }
}
