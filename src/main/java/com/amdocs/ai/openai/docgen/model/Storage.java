package com.amdocs.ai.openai.docgen.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class Storage {
    @Setter
    @Getter
    private String value;
    @Getter
    private String representation = "storage";
}
