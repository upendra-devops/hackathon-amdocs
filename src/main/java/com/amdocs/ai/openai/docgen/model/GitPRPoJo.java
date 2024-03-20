package com.amdocs.ai.openai.docgen.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
public class GitPRPoJo {

    private String jiraId;

    private String user;

    private String pullNo;

    private String repositoryName;

    private String baseSHA;

    private String headSHA;

    @Setter
    private String jiraStory;

    @Setter
    private List<CodeFragment> codeFragments;

    @Setter
    private List<GitCommitResource> committedFiles;
}
