package com.amdocs.ai.openai.docgen.git;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "gitRawClient", value = "gitRawClient", url = "https://github.com/")
public interface GitRawClient {

    @RequestMapping(method = RequestMethod.GET, value = "{user}/{repo}/raw/{sha}/{filePath}")
    String getCode(@PathVariable String user, @PathVariable String repo, @PathVariable String sha,
                   @PathVariable String filePath, @RequestHeader("Authorization") String token);
}
