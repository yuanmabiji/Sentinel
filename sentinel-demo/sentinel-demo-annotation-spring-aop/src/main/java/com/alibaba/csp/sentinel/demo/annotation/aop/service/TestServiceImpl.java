/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.annotation.aop.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 * @author Eric Zhao
 */
@Service
public class TestServiceImpl implements TestService {

    @Override
    @SentinelResource(value = "test", blockHandler = "handleException", blockHandlerClass = {ExceptionUtil.class})
    public void test() {
        System.out.println("Test");
    }

    @Override
    @SentinelResource(value = "TestResource", fallback = "helloFallback")
    public String hello(long s) {
        if (s < 0) {
            throw new IllegalArgumentException("invalid arg");
        }
        return String.format("Hello at %d", s);
    }

    @Override
    @SentinelResource(value = "helloAnother", defaultFallback = "defaultFallback",
        exceptionsToIgnore = {IllegalStateException.class})
    public String helloAnother(String name) {
        if (name == null || "bad".equals(name)) {
            throw new IllegalArgumentException("oops");
        }
        if ("foo".equals(name)) {
            throw new IllegalStateException("oops");
        }
        return "Hello, " + name;
    }

    public String helloFallback(long s, Throwable ex) {
        // Do some log here.
        ex.printStackTrace();
        return "Oops, error occurred at " + s;
    }

    public String defaultFallback() {
        System.out.println("Go to default fallback");
        return "default_fallback";
    }


    @PostConstruct
    public void init() {
        /*List<FlowRule> flowRules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setResource("hello");
        flowRule.setCount(1);
        flowRules.add(flowRule);
        FlowRuleManager.loadRules(flowRules);*/
        loadRules();

    }

    private static void loadRules() {
        // Set up basic information, only for demo purpose. You may adjust them based on your actual environment.
        // For more information, please refer https://github.com/ctripcorp/apollo
        String appId = "sentinel-demo";
        String apolloMetaServerAddress = "http://localhost:8080";
        System.setProperty("app.id", appId);
        System.setProperty("apollo.meta", apolloMetaServerAddress);

        String namespaceName = "application";
        String flowRuleKey = "flowRules";
        // It's better to provide a meaningful default value.
        String defaultFlowRules = "[]";

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ApolloDataSource<>(namespaceName,
                flowRuleKey, defaultFlowRules, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
        }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

}
