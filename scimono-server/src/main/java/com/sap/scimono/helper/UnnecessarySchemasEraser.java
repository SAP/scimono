
package com.sap.scimono.helper;

import com.sap.scimono.entity.Resource;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UnnecessarySchemasEraser<T extends Resource<T>> {

  public T eraseAllUnnecessarySchemas(final T resource, final String coreSchema) {

    Set<String> schemasWithProvidedAttributes = resource.getExtensions().keySet();
    Predicate<String> coreSchemaOrCustomSchemaWithProvidedAttributes = schema -> schema.equals(coreSchema)
        || schemasWithProvidedAttributes.contains(schema);

    // @formatter:off
    Set<String> necessarySchemas = resource.getSchemas().stream()
        .filter(coreSchemaOrCustomSchemaWithProvidedAttributes)
        .collect(Collectors.toSet());
    // @formatter:on

    return resource.builder().setSchemas(necessarySchemas).build();
  }
}
