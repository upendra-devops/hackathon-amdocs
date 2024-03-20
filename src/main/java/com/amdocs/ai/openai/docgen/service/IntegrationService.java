package com.amdocs.ai.openai.docgen.service;

import com.amdocs.ai.openai.docgen.confluence.ConfluenceFeignClient;
import com.amdocs.ai.openai.docgen.git.GitFeignClient;
import com.amdocs.ai.openai.docgen.git.GitRawClient;
import com.amdocs.ai.openai.docgen.jira.JiraFeignClient;
import com.amdocs.ai.openai.docgen.model.CodeFragment;
import com.amdocs.ai.openai.docgen.model.GitPRPoJo;
import com.amdocs.ai.openai.docgen.model.WikiTemplate;
import com.amdocs.ai.openai.docgen.util.GitHubPayloadParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IntegrationService {

    private JiraFeignClient jiraFeignClient;
    private GitFeignClient gitFeignClient;
    private GitRawClient gitRawClient;
    private ConfluenceFeignClient confluenceFeignClient;
    private ObjectMapper objectMapper;

    @Value("${com.amdocs.jira.auth}")
    private String jiraToken;

    @Value("${com.amdocs.github.auth}")
    private String githubToken;

    public IntegrationService(JiraFeignClient jiraFeignClient, GitFeignClient gitFeignClient, GitRawClient gitRawClient, ConfluenceFeignClient confluenceFeignClient) {
        this.jiraFeignClient = jiraFeignClient;
        this.gitFeignClient = gitFeignClient;
        this.gitRawClient = gitRawClient;
        this.confluenceFeignClient = confluenceFeignClient;
        objectMapper = new ObjectMapper();
    }

    public String getJiraStory(String issueId) throws JsonProcessingException {
        String response = jiraFeignClient.getJiraStoryDescription(issueId, jiraToken);
        JsonNode node = objectMapper.readValue(response, JsonNode.class);
        return node.get("fields").get("description").toString();
    }

    public GitPRPoJo getCodeFromGit(JsonNode payload) throws JsonProcessingException {
        GitPRPoJo gitPRPoJo = GitHubPayloadParser.parseInitialPayload(payload);
        gitPRPoJo.setJiraStory(this.getJiraStory(gitPRPoJo.getJiraId()));
        GitHubPayloadParser.parseFilesPayload(gitFeignClient.getFiles(gitPRPoJo.getRepositoryName(), gitPRPoJo.getPullNo(), gitPRPoJo.getUser(), githubToken), gitPRPoJo);
        gitPRPoJo.setCodeFragments(getCodeFromGit(gitPRPoJo));
        return gitPRPoJo;
    }

    public void createWiki(WikiTemplate wikiTemplate) throws JsonProcessingException {
        log.info(objectMapper.writeValueAsString(wikiTemplate));
        confluenceFeignClient.createWiki(objectMapper.writeValueAsString(wikiTemplate), jiraToken);
    }

    private List<CodeFragment> getCodeFromGit(GitPRPoJo gitPRPoJo) {
        String user = gitPRPoJo.getUser();
        String repo = gitPRPoJo.getRepositoryName();
        String baseSHA = gitPRPoJo.getBaseSHA();
        String headSHA = gitPRPoJo.getHeadSHA();

        return gitPRPoJo.getCommittedFiles().stream().map( gitCommitResource -> {
            String newCode = gitRawClient.getCode(user, repo, headSHA, gitCommitResource.getFilePath(), githubToken);
            if(gitCommitResource.isNew()) {
                return CodeFragment.builder().newCLass(newCode).fullyQualifiedClassName(gitCommitResource.getFilePath()).build();
            } else {
                String oldCode = gitRawClient.getCode(user, repo, baseSHA, gitCommitResource.getFilePath(), githubToken);
                return CodeFragment.builder().newCLass(newCode).oldCLass(oldCode).fullyQualifiedClassName(gitCommitResource.getFilePath()).build();
            }
        }).collect(Collectors.toList());
    }
}
