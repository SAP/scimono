package com.sap.scimono.scim.system.tests.launcher;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertyFileDiscoverySelector implements TestMethodsSelectorsCreator{
  private static final String METHODS_SPLIT_DELIMITER = ",";
  private final Map<Class<?>, Set<String>> methods;

  private PropertyFileDiscoverySelector(Map<Class<?>, Set<String>> methods) {
    this.methods = methods;
  }

  @Override
  public List<DiscoverySelector> createDiscoverySelectors() {
    // @formatter:off
    return methods.entrySet().stream()
        .flatMap(classMethodsEntry ->
            classMethodsEntry.getValue().stream()
                .map(methodNames -> new AbstractMap.SimpleEntry<>(classMethodsEntry.getKey(), methodNames))
        )
        .map(classStringSimpleEntry -> DiscoverySelectors.selectMethod(classStringSimpleEntry.getKey(), classStringSimpleEntry.getValue()))
        .collect(Collectors.toList());
    // @formatter:on
  }

  public static PropertyFileDiscoverySelector load(String propertyFilePath) {
    try {
      Properties properties = new Properties();
      properties.load(new FileInputStream(propertyFilePath));

      Map<Class<?>, Set<String>> methods = new HashMap<>();

      for (String className : properties.stringPropertyNames()) {
        Class<?> testClass = validateTestClassAndGet(className);
        Set<String> testMethods = validateMethodsAndGet(testClass, properties.getProperty(className));

        methods.put(testClass, testMethods);
      }


      return new PropertyFileDiscoverySelector(methods);
    } catch (IOException e) {
      throw new LauncherInitializationException("Cannot read properties file");
    }
  }

  private static Class<?> validateTestClassAndGet(String className) {
    if(!className.startsWith(TestMethodsSelectorsCreator.TESTS_BASE_PACKAGE)) {
      throw new LauncherInitializationException(String.format("Test class: '%s' is not part of the compliance test classes", className));
    }
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new LauncherInitializationException(String.format("Test class: '%s' not found", className));
    }
  }
  private static Set<String> validateMethodsAndGet(Class<?> testClass, String methods) {
    Set<String> allTestMethods = Arrays.stream(testClass.getMethods()).map(Method::getName).collect(Collectors.toSet());
    Set<String> testMethods = Arrays.stream(methods.split(METHODS_SPLIT_DELIMITER)).map(String::trim).collect(Collectors.toSet());

    testMethods.forEach(testMethod -> {
      if (!allTestMethods.contains(testMethod)) {
        throw new LauncherInitializationException(String.format("Method: '%s' is not present in class '%s'", testMethod, testClass));
      }
    });

    return testMethods;
  }
}
