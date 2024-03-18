//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.amdocs.ai.openai.docgen;


import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SimpleAiController {
	private final ChatClient aiClient;
	private final String java_code = "public class Calculator {\n    \n    public static int add(int x, int y) {\n        return x+y;\n    }\n    \n    public static int subtract(int x, int y) {\n        return x-y;\n    }\n}";

	@Autowired
	public SimpleAiController(ChatClient aiClient) {
		this.aiClient = aiClient;
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

	@GetMapping({"/ai/generateTests"})
	public String test() {
		Map<String, Object> model = new HashMap();
		model.put("javaCode", "public class Calculator {\n    \n    public static int add(int x, int y) {\n        return x+y;\n    }\n    \n    public static int subtract(int x, int y) {\n        return x-y;\n    }\n}");
		PromptTemplate promptTemplate = new PromptTemplate("Generate testcases for below class in a input/output tabular format. \n\n {javaCode}", model);
		Prompt prompt = promptTemplate.create();
		return (new Completion(this.aiClient.call(prompt.toString()))).getCompletion();
	}

	@GetMapping({"/ai/roleBased"})
	public String roleBased() throws IOException {
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
		String createDocText = "Create a documentation in which will have sections as 1. Introduction, 2. Solution details, 3. How to use new functionality\n";
		UserMessage createDocMessage = new UserMessage(createDocText);
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
		List<Generation> response = this.aiClient.call(prompt).getResults();
		return (String)response.stream().map((generation) -> {
			return generation.getOutput().getContent();
		}).collect(Collectors.joining(","));
	}
}
