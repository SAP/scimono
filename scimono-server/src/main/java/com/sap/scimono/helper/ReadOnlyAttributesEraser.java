
package com.sap.scimono.helper;

import static com.sap.scimono.entity.schema.AttributeDataType.COMPLEX;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import com.sap.scimono.callback.schemas.SchemasCallback;
import com.sap.scimono.entity.EnterpriseExtension;
import com.sap.scimono.entity.Resource;
import com.sap.scimono.entity.base.Extension;
import com.sap.scimono.entity.schema.Attribute;
import com.sap.scimono.entity.schema.Schema;
import com.sap.scimono.exception.SCIMException;

public class ReadOnlyAttributesEraser<T extends Resource<T>> {

  private SchemasCallback schemaAPI;

  public ReadOnlyAttributesEraser(final SchemasCallback schemaAPI) {
    this.schemaAPI = schemaAPI;
  }

  public T eraseAllFormCustomExtensions(final T resource) {

    List<Extension> extensions = resource.getExtensions().values().stream().map(extension -> {
      if (extension instanceof EnterpriseExtension) {
//        https://github.com/SAP/scimono/issues/77

//        EnterpriseExtension enterpriseExtension = (EnterpriseExtension) extension;
//        Manager manager = enterpriseExtension.getManager();
//        if (manager != null) {
//          Manager managerWithoutDisplayName = new Manager.Builder().setDisplayName(null).build();
//          return new EnterpriseExtension.Builder(enterpriseExtension).setManager(managerWithoutDisplayName).build();
//        }

        return extension;
      }

      Map<String, Object> attributes = extension.getAttributes();
      Schema customSchema = schemaAPI.getSchema(extension.getUrn());
      if (customSchema == null) {
        throw new SCIMException(SCIMException.Type.INVALID_SYNTAX, String.format("Schema '%s' does not exist.", extension.getUrn()),
            Response.Status.BAD_REQUEST);
      }
      removeReadOnlyAttributes(customSchema.toAttribute(), attributes);

      return new Extension.Builder(extension).setAttributes(attributes).build();
    }).collect(Collectors.toList());

    return resource.builder().removeExtensions().addExtensions(extensions).build();
  }

  private boolean removeReadOnlyAttributes(final Attribute targetAttribute, final Object value) {
    if ("readOnly".equals(targetAttribute.getMutability())) {
      return true;
    }

    if (!COMPLEX.toString().equals(targetAttribute.getType())) {
      return false;
    }

    if (targetAttribute.isMultiValued()) {
      if (value instanceof Collection) {
        @SuppressWarnings("unchecked")
        Collection<Object> valueCollection = (Collection<Object>) value;

        // @formatter:off
        Attribute singleValuedAttribute = new Attribute.Builder()
            .name(targetAttribute.getName())
            .multiValued(false)
            .type(targetAttribute.getType())
            .mutability(targetAttribute.getMutability())
            .addSubAttributes(targetAttribute.getSubAttributes())
            .build();

        valueCollection.removeAll(valueCollection.stream()
            .filter(object -> removeReadOnlyAttributes(singleValuedAttribute, object))
            .collect(Collectors.toList()));
        // @formatter:on
      } else {
        throw new SCIMException(SCIMException.Type.INVALID_SYNTAX,
            String.format("Provided attribute with name '%s' is array according to the schema", targetAttribute.getName()),
            Response.Status.BAD_REQUEST);
      }
    }

    if (value instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> valueMap = (Map<String, Object>) value;

      Iterator<Entry<String, Object>> iterator = valueMap.entrySet().iterator();
      while(iterator.hasNext()) {
        Map.Entry<String, Object> entry = iterator.next();
        // @formatter:off
        Attribute subAttribute = targetAttribute.getSubAttributes().stream()
            .filter(attribute -> entry.getKey().equalsIgnoreCase(attribute.getName()))
            .findAny()
            .orElseThrow(() -> new SCIMException(SCIMException.Type.INVALID_SYNTAX,
                String.format("Provided attribute with name '%s' does not exist according to the schema", entry.getKey()),
                Response.Status.BAD_REQUEST));
        // @formatter:on
        if (removeReadOnlyAttributes(subAttribute, entry.getValue())) {
          iterator.remove();
        }
      }
    }

    return false;
  }
}
