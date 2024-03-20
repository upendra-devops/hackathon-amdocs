package com.amdocs.ai.openai.docgen.util;

import com.amdocs.ai.openai.docgen.model.GitCommitResource;
import com.amdocs.ai.openai.docgen.model.GitPRPoJo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitHubPayloadParser {

    private static Map<String, String> info = new HashMap<>();
    private static Set<String> commitMessages = new HashSet<>();
    private static Set<String> addedFiles = new HashSet<>();
    private static Set<String> modifiedFiles = new HashSet<>();

    public static GitPRPoJo parseInitialPayload(JsonNode rootNode) {

        String jiraId = Arrays.stream(rootNode.get("pull_request").get("title").asText().split(" ")).filter(s -> s.startsWith("AID-")).findFirst().orElse("JIRA ID Not Found");
        String username = rootNode.get("pull_request").get("head").get("user").get("login").asText();
        String pullNo = rootNode.get("number").asText();
        String repo = rootNode.get("repository").get("name").asText();
        String baseSHA = rootNode.get("pull_request").get("base").get("sha").asText();
        String headSHA = rootNode.get("pull_request").get("head").get("sha").asText();
        return GitPRPoJo.builder()
                .jiraId(jiraId)
                .user(username)
                .pullNo(pullNo)
                .repositoryName(repo)
                .baseSHA(baseSHA)
                .headSHA(headSHA).build();
    }

    public static void parseFilesPayload(JsonNode filesNode, GitPRPoJo gitPRPoJo) {
        List<GitCommitResource> poJoList = StreamSupport.stream(filesNode.spliterator(), false)
                .filter(node -> !node.get("filename").asText().endsWith("txt") || !node.get("filename").asText().endsWith("om.xml"))
                .map(node -> GitCommitResource.builder()
                        .filePath(node.get("filename").asText())
                        .isNew("added".equals(node.get("status").asText())).build()).collect(Collectors.toList());
        gitPRPoJo.setCommittedFiles(poJoList);
    }

    public static void main(String[] args) throws IOException {
        String payloadStr = readFromInputStream(new FileInputStream("/Users/hajoshi/Gen AI Dev/repo/hackathon/src/main/resources/gitPayload.txt"));
        String filesPayloadStr = readFromInputStream(new FileInputStream("/Users/hajoshi/Gen AI Dev/repo/hackathon/src/main/resources/filesPayload.txt"));
        ObjectMapper objectMapper = new ObjectMapper();

        /*JsonNode node = objectMapper.readValue(payloadStr, JsonNode.class);
        System.out.println(node.get("action").asText());
        System.out.println(node.get("number").asInt());
        System.out.println(node.get("pull_request").get("title").asText());
        System.out.println(node.get("pull_request").get("head").get("sha").asText());
        System.out.println(node.get("pull_request").get("base").get("sha").asText());
        System.out.println(node.get("pull_request").get("head").get("user").get("login").asText());*/

        JsonNode fileNode = objectMapper.readValue(filesPayloadStr, JsonNode.class);
        StreamSupport.stream(fileNode.spliterator(), false).forEach(node -> {
            System.out.println(node.get("filename"));
            System.out.println(node.get("status"));
        });

        //GitPRPoJo gitPRData = parse(node);
        //System.out.println(gitPRData);
    }

    private static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}
