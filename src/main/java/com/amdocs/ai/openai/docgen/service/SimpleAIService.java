package com.amdocs.ai.openai.docgen.service;

import com.amdocs.ai.openai.docgen.model.CodeFragment;
import com.amdocs.ai.openai.docgen.model.DocuFormat;
import com.amdocs.ai.openai.docgen.model.TestCaseFormat;
import com.amdocs.ai.openai.docgen.util.JSONParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SimpleAIService {

    private final ChatClient aiClient;
    private final OpenAiChatOptions customChatOptions;
    private static final UserMessage jiraStoryPrompt = new UserMessage("Jira story is as follows.");
    private static final UserMessage javaCodePrompt = new UserMessage("Code is as follows.");
    private static final String EXISTING_JAVA_CODE_TEXT = "{classname} is existing class, {old} is previous version and {new} is its latest version.\n";
    private static final String NEW_JAVA_CODE_TEXT = "{classname} is newly added, its code is {new}.\n";
    private static final String DOC_SYSTEM_TEXT = "You are a helpful AI assistant that helps in generating non technical documentation for java code.\nThe code is written using java and Spring framework.\nYou should expect jira story and java code to know the development.\n";
    private static final String TEST_SYSTEM_TEXT = "You are a helpful AI assistant that helps in generating manual or gherkin testcases. \nYou should expect jira story.";
    private static final String CREATE_DOC_TEXT = "Create a documentation in non technical fashion having sections as \n" +
            "1. Introduction section gives brief idea about the functionality.\n " +
            "2. Solution section gives details explanation about the solution. \n" +
            "3. Adoption section will have technical explanation about using/adopting new functionality.\n {format}";
    private static final String CREATE_TEST_TEXT = "Create {type} testcases for a user story {story}. \n {testcaseformat}";

    public SimpleAIService(ChatClient aiClient) {
        this.aiClient = aiClient;
        customChatOptions = new OpenAiChatOptions();
        customChatOptions.setTemperature(0F);
    }

    public DocuFormat getDocumentation(UserMessage jiraStory, List<CodeFragment> codeFragments) throws JsonProcessingException {
        var docOutputParser = new BeanOutputParser<>(DocuFormat.class);
        List<Message> messages = new ArrayList();

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(DOC_SYSTEM_TEXT);
        Message systemMessage = systemPromptTemplate.createMessage();

        Message createDocMessage = new PromptTemplate(CREATE_DOC_TEXT, Map.of("format", docOutputParser.getFormat())).createMessage();

        messages.add(systemMessage);
        messages.add(jiraStoryPrompt);
        messages.add(jiraStory);
        messages.add(javaCodePrompt);
        messages.addAll(getJavaCodeMessages(codeFragments));
        messages.add(createDocMessage);

        Prompt prompt = new Prompt(messages, customChatOptions);
        log.info(prompt.getContents());
        Generation response = this.aiClient.call(prompt).getResult();

        DocuFormat localParserOut = JSONParser.parse(response.getOutput().getContent());
        log.info(localParserOut.getIntroduction().getDescription());
        log.info(localParserOut.getSolution().getDescription());
        log.info(localParserOut.getAdoption().getDescription());
        return localParserOut;
    }

    public TestCaseFormat getTestCases(UserMessage jiraStory, String type) {
        var testOutParser = new BeanOutputParser<>(TestCaseFormat.class);
        List<Message> messages = new ArrayList();

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(TEST_SYSTEM_TEXT);
        Message systemMessage = systemPromptTemplate.createMessage();
        messages.add(systemMessage);

        Message createTestsMessage = new PromptTemplate(CREATE_TEST_TEXT, Map.of("story", jiraStory, "testcaseformat", testOutParser.getFormat(), "type", type)).createMessage();
        messages.add(createTestsMessage);
        Prompt prompt = new Prompt(messages, customChatOptions);
        Generation response = this.aiClient.call(prompt).getResult();
        log.info(response.getOutput().getContent());
        return testOutParser.parse(response.getOutput().getContent());
    }

    private Collection<? extends Message> getJavaCodeMessages(List<CodeFragment> codeFragments) {
        List<Message> javaCodeMessages = new ArrayList<>();

        javaCodeMessages = codeFragments.stream().map(codeFragment -> {
            if(codeFragment.isNewAddition()) {
                return new PromptTemplate(NEW_JAVA_CODE_TEXT,
                        Map.of("classname", codeFragment.getFullyQualifiedClassName(), "new", codeFragment.getNewCLass()))
                        .createMessage();
            } else {
                return new PromptTemplate(EXISTING_JAVA_CODE_TEXT,
                        Map.of("classname", codeFragment.getFullyQualifiedClassName(), "new", codeFragment.getNewCLass(), "old", codeFragment.getOldCLass()))
                        .createMessage();
            }
        }).collect(Collectors.toList());
        return javaCodeMessages;
    }
}
