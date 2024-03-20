package com.amdocs.ai.openai.docgen.git;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "gitClient", value = "gitClient", url = "https://github.com/")
public interface GitFeignClient {

    @RequestMapping(method = RequestMethod.GET, value = "api/v3/repos/{user}/{repo}/pulls/{pullNo}/files")
    ArrayNode getFiles(@PathVariable String repo, @PathVariable String pullNo, @PathVariable String user,
                         @RequestHeader("Authorization") String token);

    @RequestMapping(method = RequestMethod.GET, value = "raw/{user}/{repo}/{sha}/{filePath}")
    String getCode(@PathVariable String user, @PathVariable String repo, @PathVariable String sha,
                   @PathVariable String filePath, @RequestHeader("Authorization") String token);
}
