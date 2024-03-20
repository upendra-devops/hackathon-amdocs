package com.amdocs.ai.openai.docgen.confluence;

import com.fasterxml.jackson.databind.JsonNode;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "confluenceClient", value = "confluenceClient", url = "https://aidocs.atlassian.net/")
public interface ConfluenceFeignClient {

    @PostMapping(value = "wiki/rest/api/content", consumes = "application/json", produces = "application/json")
    JsonNode createWiki(@RequestBody String wikiContent, @RequestHeader("Authorization") String token);
}
