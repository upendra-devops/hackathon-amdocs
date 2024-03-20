package com.amdocs.ai.openai.docgen.jira;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "jiraClient", value = "jiraClient", url = "https://aidocs.atlassian.net/")
public interface JiraFeignClient {

    //static final String TOKEN = "Basic YW50cmkucGFuYWdpb3RvdUB2dWJpcXVpdHkubmV0OkFUQVRUM3hGZkdGMDF4OHYwdEZ2RzM4clBKejlWUEhldWZ6UEc1ZGFKTWNaWm1YN1ZvTGxkOEY3dkc2ZE90andjblVleWRqbzJFdE1DNHAxMm9GSlkwNnpTNzV5LWZLOTZjY3ktbUI2LU5seDgybmotVkRNdG8zb3ZTWmtjQVlzMEZscEp0R09mMlBYNkZkOFVMS2lteWpwSnRiWDFTMDRQWEhRSThTb1Q1SjdKeWdDNE1ESzZLZz04QUZDMkQzNQ==";

    @RequestMapping(method = RequestMethod.GET, value = "rest/api/2/issue/{issueId}?fields=description")
    String getJiraStoryDescription(@PathVariable String issueId, @RequestHeader("Authorization") String token);
}
