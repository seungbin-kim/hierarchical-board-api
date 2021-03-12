package com.personal.board.service;

import com.personal.board.exception.BadArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PatchUtil {
  // PATCH 요청시 입력이 된 필드명들만 구분하기 위한 부분
  static <T> ArrayList<String> validateFields(final T request, final Field[] declaredFields) throws IllegalAccessException {
    ArrayList<String> validatedFields = new ArrayList<>(); // 정상적으로 입력된 필드이름을 저장하기 위한 리스트
    for (Field declaredField : declaredFields) { // 필드들 얻기
      declaredField.setAccessible(true); // 필드접근허용
      String fieldName = declaredField.getName(); // 필드이름 문자열로 얻기
      Object fieldValue = declaredField.get(request); // 필드에 입력된 값 얻기
      if (fieldValue == null) { // 입력이 안됬으면 다음필드로
        continue;
      } else if (StringUtils.isBlank(fieldValue.toString())) { // 입력이 됬지만 비어있다면 예외발생
        throw new BadArgumentException(fieldName + " is blank.");
      }
      validatedFields.add(fieldName); // 정상적으로 내용이 있다면 입력된 해당 필드이름 저장
    }
    return validatedFields; // 정상 입력된 필드이름들 리턴
  }

}
