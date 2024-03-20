package com.amdocs.ai.openai.docgen.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.messages.UserMessage;

@Getter
@Builder
public class CodeFragment {
    private String fullyQualifiedClassName;

    private String oldCLass;

    private String newCLass;

    public boolean isNewAddition() {
        return oldCLass == null;
    }
}
