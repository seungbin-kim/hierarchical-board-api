package com.personal.board.service;

import com.personal.board.exception.BadArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PatchUtil {

  static <T> ArrayList<String> validateFields(final T request, final Field[] declaredFields) throws IllegalAccessException {
    ArrayList<String> validatedFields = new ArrayList<>();
    for (Field declaredField : declaredFields) {
      declaredField.setAccessible(true);
      String fieldName = declaredField.getName();
      Object fieldValue = declaredField.get(request);
      if (fieldValue == null) {
        continue;
      } else if (StringUtils.isBlank(fieldValue.toString())) {
        throw new BadArgumentException(fieldName + " is blank.");
      }
      validatedFields.add(fieldName);
    }
    return validatedFields;
  }

}
