package com.amdocs.ai.openai.docgen.model;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class Section implements Serializable {
    private String title;

    private String description;
}
