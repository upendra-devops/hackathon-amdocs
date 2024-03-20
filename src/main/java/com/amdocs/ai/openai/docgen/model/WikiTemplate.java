package com.amdocs.ai.openai.docgen.model;

import lombok.Getter;
import lombok.Setter;

public class WikiTemplate {

    @Setter
    @Getter
    private String type;

    @Setter
    @Getter
    private String title;

    @Setter
    @Getter
    private Ancestor ancestors[];

    @Setter
    @Getter
    private Space space;

    @Setter
    @Getter
    private Body body;
}
