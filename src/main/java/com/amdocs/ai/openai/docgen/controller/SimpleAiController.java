//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.amdocs.ai.openai.docgen.controller;


import com.amdocs.ai.openai.docgen.model.Ancestor;
import com.amdocs.ai.openai.docgen.model.Body;
import com.amdocs.ai.openai.docgen.model.CodeFragment;
import com.amdocs.ai.openai.docgen.model.Completion;
import com.amdocs.ai.openai.docgen.model.DocuFormat;
import com.amdocs.ai.openai.docgen.model.GitPRPoJo;
import com.amdocs.ai.openai.docgen.model.Section;
import com.amdocs.ai.openai.docgen.model.Space;
import com.amdocs.ai.openai.docgen.model.Storage;
import com.amdocs.ai.openai.docgen.model.WikiTemplate;
import com.amdocs.ai.openai.docgen.service.IntegrationService;
import com.amdocs.ai.openai.docgen.service.SimpleAIService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class SimpleAiController {
	private final ChatClient aiClient;
	private final SimpleAIService simpleAIService;
	private final IntegrationService integrationService;
	private final ObjectMapper om;

	@Autowired
	public SimpleAiController(ChatClient aiClient, SimpleAIService simpleAIService, IntegrationService integrationService) {
		this.aiClient = aiClient;
		this.simpleAIService = simpleAIService;
		this.integrationService = integrationService;
		om = new ObjectMapper();
		om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
	}

	@GetMapping({"/ai/simple"})
	public Completion completion(@RequestParam(value = "message",defaultValue = "Tell me a joke") String message) {
		return new Completion(this.aiClient.call(message));
	}

	@GetMapping({"/ai/generateDocs"})
	public String docs() {
		Map<String, Object> model = new HashMap();
		model.put("javaCode", "public class Calculator {\n    \n    public static int add(int x, int y) {\n        return x+y;\n    }\n    \n    public static int subtract(int x, int y) {\n        return x-y;\n    }\n}");
		PromptTemplate promptTemplate = new PromptTemplate("Generate documentation for below class in a non technical format. \n\n {javaCode}", model);
		Prompt prompt = promptTemplate.create();
		return (new Completion(this.aiClient.call(prompt.toString()))).getCompletion();
	}

	@GetMapping({"/ai/jira"})
	public String jira() throws JsonProcessingException {
		return new Completion(integrationService.getJiraStory("AID-3")).getCompletion();
	}

	@GetMapping(value = {"/ai/roleBased"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public Completion roleBased() throws IOException {
		List<Message> messages = new ArrayList();
		File resource1 = ResourceUtils.getFile("classpath:story_1/Customer.java");
		File resource2 = ResourceUtils.getFile("classpath:story_1/CustomerController.java");
		File resource3 = ResourceUtils.getFile("classpath:story_1/CustomerRepository.java");
		File resource4 = ResourceUtils.getFile("classpath:story_1/CustomerService.java");
		File resource5 = ResourceUtils.getFile("classpath:story_1/story1.txt");
		Message userMessage1 = new UserMessage(new InputStreamResource(new FileInputStream(resource1)));
		Message userMessage2 = new UserMessage(new InputStreamResource(new FileInputStream(resource2)));
		Message userMessage3 = new UserMessage(new InputStreamResource(new FileInputStream(resource3)));
		Message userMessage4 = new UserMessage(new InputStreamResource(new FileInputStream(resource4)));
		Message userMessage5 = new UserMessage(new InputStreamResource(new FileInputStream(resource5)));
		UserMessage jiraStoryMessage = new UserMessage("Jira story is as follows.");
		UserMessage javaCodeMessage = new UserMessage("Code is as follows");
		var outputParser = new BeanOutputParser<>(DocuFormat.class);
		String createDocText = "Create a documentation in non technical fashion having sections as \n" +
				"1. Introduction section gives brief idea about the functionality.\n " +
				"2. Solution section gives details explanation about the solution. \n" +
				"3. Adoption section will have technical explanation about using/adopting new functionality.\n {format}";

		Message createDocMessage = new PromptTemplate(createDocText, Map.of("format", outputParser.getFormat())).createMessage();
		String systemText = "You are a helpful AI assistant that helps in generating non technical documentation for java code.\nThe code is written using java and Spring framework.\nYou should expect jira story and java code to know the development.\n";
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
		Message systemMessage = systemPromptTemplate.createMessage();
		messages.add(systemMessage);
		messages.add(jiraStoryMessage);
		messages.add(userMessage5);
		messages.add(javaCodeMessage);
		messages.add(userMessage1);
		messages.add(userMessage2);
		messages.add(userMessage3);
		messages.add(userMessage4);
		messages.add(createDocMessage);
		Prompt prompt = new Prompt(messages);

		Generation response = this.aiClient.call(prompt).getResult();

		return new Completion(this.aiClient.call(response.getOutput().getContent()));
	}

	@GetMapping(value = {"/ai/generateDocumentation"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public Completion generateDocumentation() throws FileNotFoundException, JsonProcessingException {
		File resource1Old = ResourceUtils.getFile("classpath:story_1/CustomerController.java");
		File resource2Old = ResourceUtils.getFile("classpath:story_1/CustomerService.java");
		File resource1New = ResourceUtils.getFile("classpath:story_2/CustomerController.java");
		File resource2New = ResourceUtils.getFile("classpath:story_2/CustomerService.java");
		File story = ResourceUtils.getFile("classpath:story_2/story2.txt");

		UserMessage storyMessage = new UserMessage(new InputStreamResource(new FileInputStream(story)));

		List<CodeFragment> codeFragments = new ArrayList<>();
		codeFragments.add(getCodeFragment("CustomerController", resource1Old, resource1New));
		codeFragments.add(getCodeFragment("CustomerService", resource2Old, resource2New));

		return new Completion(om.writeValueAsString(simpleAIService.getDocumentation(storyMessage, codeFragments)));
	}

	@GetMapping(value = {"/ai/generateTestcases"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public Completion generateTestCases() throws FileNotFoundException, JsonProcessingException {
		File story = ResourceUtils.getFile("classpath:story_2/story2.txt");
		UserMessage storyMessage = new UserMessage(new InputStreamResource(new FileInputStream(story)));

		return new Completion(om.writeValueAsString(simpleAIService.getTestCases(storyMessage, "manual")));
	}

	@PostMapping({"/ai/trigger"})
	public ResponseEntity trigger(@RequestBody JsonNode payload) throws JsonProcessingException {
		//log.info(payload.toPrettyString());
		GitPRPoJo gitPRPoJo = integrationService.getCodeFromGit(payload);
		UserMessage storyMessage = new UserMessage(gitPRPoJo.getJiraStory());
		DocuFormat docuFormat = simpleAIService.getDocumentation(storyMessage, gitPRPoJo.getCodeFragments());

		Storage storage = new Storage();
		storage.setValue("<p><b>Introduction</b></p><p>"+docuFormat.getIntroduction().getDescription()+
				"</p><p><b>Solution details</b></p><p>"+docuFormat.getSolution().getDescription()+
				"</p><p><b>Adopting new functionality</b></p><p>"+docuFormat.getAdoption().getDescription()+".</p>");
		WikiTemplate wikiTemplate = new WikiTemplate();
		wikiTemplate.setType("page");
		wikiTemplate.setTitle(gitPRPoJo.getJiraId());
		Ancestor ancestor = new Ancestor();
		ancestor.setId(851970);
		wikiTemplate.setAncestors(new Ancestor[]{ancestor});
		Space space = new Space();
		space.setKey("AIDOCS");
		wikiTemplate.setSpace(space);
		Body body = new Body();
		body.setStorage(storage);
		wikiTemplate.setBody(body);
		integrationService.createWiki(wikiTemplate);
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = {"/ai/gingertrigger/{type}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public Completion gingerTrigger(@PathVariable String type, @RequestBody String story) throws JsonProcessingException {
		log.info(story);

		UserMessage storyMessage = new UserMessage(new InputStreamResource(new ByteArrayInputStream(story.getBytes(StandardCharsets.UTF_8))));

		return new Completion(om.writeValueAsString(simpleAIService.getTestCases(storyMessage, type)));
	}

	private CodeFragment getCodeFragment(String className, File resourceOld, File resourceNew) throws FileNotFoundException {
		return CodeFragment.builder().fullyQualifiedClassName(className)
				.oldCLass(new UserMessage(new InputStreamResource(new FileInputStream(resourceOld))).getContent())
				.newCLass(new UserMessage(new InputStreamResource(new FileInputStream(resourceNew))).getContent())
				.build();
	}
}
