package com.amdocs.ai.openai.docgen.model;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class DocuFormat implements Serializable {

    private Section introduction;

    private Section solution;

    private Section adoption;
}
