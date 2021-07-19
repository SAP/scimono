package com.sap.scimono.entity.bulk;

import com.sap.scimono.exception.InternalScimonoException;

public class BulkOperationCastingException extends InternalScimonoException {
  private static final String MESSAGE_PATTERN = "Unable to cast bulk operation with bulkId %s to %s";

  public BulkOperationCastingException(String bulkId, Class<?> clazz) {
    super(String.format(MESSAGE_PATTERN, bulkId, clazz));
  }
}
