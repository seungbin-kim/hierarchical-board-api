package com.personal.board.util;

import com.personal.board.exception.BadArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;

@Component
public class PatchUtil {

  /**
   * 업데이트 요청에서 유효한 필드목록 반환
   * @param request 업데이트 정보
   * @return 유효한 필드목록
   * @throws IllegalAccessException 필드 접근 불가시 발생
   */
  public <T> ArrayList<String> getValidatedFields(final T request) throws IllegalAccessException {

    Field[] declaredFields = request.getClass().getDeclaredFields();
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
