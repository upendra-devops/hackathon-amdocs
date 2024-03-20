package com.amdocs.ai.openai.docgen.git;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "gitClient", value = "gitClient", url = "https://api.github.com/")
public interface GitFeignClient {

    @RequestMapping(method = RequestMethod.GET, value = "repos/{user}/{repo}/pulls/{pullNo}/files")
    ArrayNode getFiles(@PathVariable String repo, @PathVariable String pullNo, @PathVariable String user,
                         @RequestHeader("Authorization") String token);
}
