package com.amdocs.ai.openai.docgen.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class GitCommitResource {
    private boolean isNew;

    private String filePath;
}
