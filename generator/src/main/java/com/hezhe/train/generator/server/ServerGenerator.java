package com.hezhe.train.generator.server;

import com.hezhe.train.generator.util.FreemarkerUtil;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerGenerator {
    static String toPath = "generator/src/main/java/com/hezhe/train/generator/test/";

    static {
        new File(toPath).mkdirs();
    }

    public static void main(String[] args) throws IOException, TemplateException {
        FreemarkerUtil.initConfig("test.ftl");
        Map<String, Object> param = new HashMap<>();
//        param.put("basePackage", "com.hezhe.train.generator.server");
        param.put("domain", "Test");
        FreemarkerUtil.generator(toPath + "Test.java", param);
    }
}
