package com.paly.legend.character;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CreateCharacterRequest {

    @NotBlank
    @Size(min = 2, max = 12)
    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9_]+$", message = "只能包含中文、字母、数字和下划线")
    private String nickname;

    private String className;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}

