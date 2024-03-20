package com.amdocs.ai.openai.docgen.util;

import com.amdocs.ai.openai.docgen.model.Ancestor;
import com.amdocs.ai.openai.docgen.model.Body;
import com.amdocs.ai.openai.docgen.model.DocuFormat;
import com.amdocs.ai.openai.docgen.model.Section;
import com.amdocs.ai.openai.docgen.model.Space;
import com.amdocs.ai.openai.docgen.model.Storage;
import com.amdocs.ai.openai.docgen.model.WikiTemplate;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JSONParser {

    private static List<String> sections = Arrays.asList("Introduction", "Solution", "Adoption", "introduction", "solution", "adoption");
    private static Map<String, String> sectionMap = new HashMap<>();



    public static DocuFormat parse(String text) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode node = objectMapper.readValue(text, JsonNode.class);
        Iterator<Map.Entry<String, JsonNode>> nodeItr = node.fields();
        while (nodeItr.hasNext()) {
            Map.Entry<String, JsonNode> nextNode = nodeItr.next();
            if("$schema".equals(nextNode.getKey()) || "type".equals(nextNode.getKey()) || nextNode.getValue().isValueNode()) {
                continue;
            }
            parse(nextNode);
        }

        DocuFormat object = DocuFormat.builder()
                .introduction(Section.builder().title("Introduction").description(sectionMap.get("Introduction".toLowerCase(Locale.ROOT))).build())
                .solution(Section.builder().title("Solution details").description(sectionMap.get("Solution".toLowerCase(Locale.ROOT))).build())
                .adoption(Section.builder().title("Adopting new functionality").description(sectionMap.get("Adoption".toLowerCase(Locale.ROOT))).build())
                .build();

        return object;
    }

    private static void parse(Map.Entry<String, JsonNode> objNode) {
        if(sections.contains(objNode.getKey())) {
            String sectionKey = sections.stream().filter(section -> section.equals(objNode.getKey())).findFirst().get();

            sectionMap.put(sectionKey.toLowerCase(Locale.ROOT), declutterContent(objNode.getValue()));
            return;
        }
        Iterator<Map.Entry<String, JsonNode>> nodeItr = objNode.getValue().fields();
        while(nodeItr.hasNext()) {
            parse(nodeItr.next());
        }
    }

    private static String declutterContent(JsonNode value) {
        StringBuffer stringBuffer = new StringBuffer();
        if(value.isValueNode()) {
            stringBuffer.append(value.toPrettyString());
            stringBuffer.append("\n");
        } else {
            Iterator<Map.Entry<String, JsonNode>> fields = value.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> node = fields.next();
                if("type".equals(node.getKey())) {
                    continue;
                }

                if(node.getValue().isValueNode()) {
                    stringBuffer.append(node.getValue().toPrettyString());
                    stringBuffer.append("\n");
                }

                if(value.isContainerNode()) {
                    String output = declutterContent(node.getValue());
                }
            }
        }

        return stringBuffer.toString();
    }

    public static void main(String[] args) throws JsonProcessingException {
        String json = "{\n" +
                "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"sections\": {\n" +
                "    \"Introduction\": {\n" +
                "      \"content\": \"The API allows consumers to update customer information while ensuring that the first name and last name remain unchanged.\"\n" +
                "    },\n" +
                "    \"Solution\": {\n" +
                "      \"content\": \"The solution involves updating customer information via HTTP PUT or PATCH requests to the /customers/{customerId} endpoint. The API validates the request payload, updates the customer record, and returns appropriate status codes and error messages as needed.\"\n" +
                "    },\n" +
                "    \"Adoption\": {\n" +
                "      \"content\": \"To adopt the new functionality, developers can utilize the updated CustomerController and CustomerService classes. They need to send PUT or PATCH requests with the desired customer information to update records. The API ensures data integrity by not allowing changes to the first name and last name.\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //DocuFormat object = parse(json);
        ObjectMapper om = new ObjectMapper();
        //om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        DocuFormat docuFormat = DocuFormat.builder().introduction(Section.builder().description("Introduction").title("Introduction").build())
                .solution(Section.builder().title("Solution").description("Solution").build())
                .adoption(Section.builder().title("Adoption").description("Adoption").build()).build();
        Storage storage = new Storage();
        storage.setValue("<p><b>Introduction</b></p><p>"+docuFormat.getIntroduction().getDescription()+
                "</p><p><b>Solution details</b></p><p>"+docuFormat.getSolution().getDescription()+
                "</p><p><b>Adopting new functionality</b></p><p>"+docuFormat.getAdoption().getDescription()+".</p>");
        WikiTemplate wikiTemplate = new WikiTemplate();
        wikiTemplate.setType("page");
        //wikiTemplate.setTitle(gitPRPoJo.getJiraId());
        wikiTemplate.setTitle("Story 007");
        Ancestor ancestor = new Ancestor();
        ancestor.setId(851970);
        wikiTemplate.setAncestors(new Ancestor[]{ancestor});
        Space space = new Space();
        space.setKey("AIDOCS");
        wikiTemplate.setSpace(space);
        Body body = new Body();
        body.setStorage(storage);
        wikiTemplate.setBody(body);
        System.out.println(om.writeValueAsString(wikiTemplate));
    }
}
