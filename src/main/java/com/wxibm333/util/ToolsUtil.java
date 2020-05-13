package com.wxibm333.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * @author wangXin
 * @version v1.0.0
 * @date 2020-05-11 15:23
 */
public class ToolsUtil {

  private final static String PATTERN = "yyyy-MM-dd HH:mm:ss";
  private final static DateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN);
  private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss");
  private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
  private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Map<String, Object> NORMAL_TYPES = new HashMap<>();

  static {
    NORMAL_TYPES.put("java.lang.Boolean", false);
    NORMAL_TYPES.put("java.lang.Byte", 0);
    NORMAL_TYPES.put("java.lang.Short", (short) 0);
    NORMAL_TYPES.put("java.lang.Integer", 0);
    NORMAL_TYPES.put("java.lang.Long", 0L);
    NORMAL_TYPES.put("java.lang.Float", 0.0F);
    NORMAL_TYPES.put("java.lang.Double", 0.0D);
    NORMAL_TYPES.put("java.lang.String", "");
    NORMAL_TYPES.put("java.math.BigDecimal", 0.0);
    NORMAL_TYPES.put("java.util.Date", DATE_FORMAT.format(new Date()));
    // NORMAL_TYPES.put("java.time.Timestamp", System.currentTimeMillis());
    NORMAL_TYPES.put("java.time.LocalDate", DATE_FORMATTER.format(LocalDate.now()));
    NORMAL_TYPES.put("java.time.LocalTime", TIME_FORMATTER.format(LocalTime.now()));
    NORMAL_TYPES.put("java.time.LocalDateTime", DATE_TIME_FORMATTER.format(LocalDateTime.now()));
  }

  public static boolean isNormalType(PsiClass psiClass) {
    return NORMAL_TYPES.containsKey(psiClass.getQualifiedName());
  }

  public static boolean isNormalType(PsiType psiType) {
    return NORMAL_TYPES.containsKey(psiType.getCanonicalText());
  }

  private static void putDefaultValueToJsonObject(Object defaultValue, String key,
      JsonObject jsonObject) {
    if (defaultValue instanceof Number) {
      jsonObject.addProperty(key, (Number) defaultValue);
    } else if (defaultValue instanceof Character) {
      jsonObject.addProperty(key, (Character) defaultValue);
    } else if (defaultValue instanceof Boolean) {
      jsonObject.addProperty(key, (Boolean) defaultValue);
    } else if (defaultValue instanceof String) {
      jsonObject.addProperty(key,
          StringUtils.isNotBlank((String) defaultValue) ? (String) defaultValue : "demoData");
    }
  }

  public static void putDefaultValueForPrimitiveOrNormalType(PsiType type, String key,
      JsonObject jsonObject) {
    Object defaultValue = null;
    if (type instanceof PsiPrimitiveType) {
      defaultValue = PsiTypesUtil.getDefaultValue(type);
    } else if (ToolsUtil.isNormalType(type)) {
      defaultValue = NORMAL_TYPES.get(type.getCanonicalText());
    }
    ToolsUtil.putDefaultValueToJsonObject(defaultValue, key, jsonObject);
  }

  private static JsonArray putDefaultValueToJsonArray(Object defaultValue) {
    JsonArray jsonArray = new JsonArray();
    if (defaultValue instanceof Number) {
      jsonArray.add((Number) defaultValue);
    } else if (defaultValue instanceof Character) {
      jsonArray.add((Character) defaultValue);
    } else if (defaultValue instanceof Boolean) {
      jsonArray.add((Boolean) defaultValue);
    } else if (defaultValue instanceof String) {
      jsonArray
          .add(StringUtils.isNotBlank((String) defaultValue) ? (String) defaultValue : "demoData");
    }
    return jsonArray;
  }

  public static JsonArray getDefaultValueForPrimitiveOrNormalType(PsiType type) {
    if (type == null) {
      return new JsonArray();
    }
    Object defaultValue = null;
    String canonicalText = type.getCanonicalText();
    if (type instanceof PsiPrimitiveType) {
      defaultValue = PsiTypesUtil.getDefaultValue(type);
    } else if (ToolsUtil.isNormalType(type)) {
      defaultValue = NORMAL_TYPES.get(canonicalText);
    }
    return ToolsUtil.putDefaultValueToJsonArray(defaultValue);
  }

  public static JsonArray getDefaultValueForPrimitiveOrNormalType(PsiClass psiClass) {
    if (ToolsUtil.isNormalType(psiClass)) {
      return ToolsUtil.putDefaultValueToJsonArray(NORMAL_TYPES.get(psiClass.getQualifiedName()));
    }
    return new JsonArray();
  }

  /**
   * 判断字段类型是否是原始数据类型以及封装类型
   *
   * @param type 字段类型
   * @return boolean
   * @author wangXin
   * @date 2020-05-12 13:43
   */
  public static boolean isPrimitiveOrNormalType(PsiType type) {
    // 如果是数组，fieldTypeName = type[]
    return type instanceof PsiPrimitiveType || ToolsUtil.isNormalType(type);
  }

  /**
   * 判断对象是否是集合类
   *
   * @param psiClass class对象
   * @return boolean
   * @author wangXin
   * @date 2020-05-13 13:35
   */
  public static boolean isCollectionType(PsiClass psiClass) {
    PsiClass[] classSupers = psiClass.getSupers();
    for (PsiClass superClass : classSupers) {
      String qualifiedName = superClass.getQualifiedName();
      boolean isCollection = CommonClassNames.JAVA_LANG_ITERABLE.equals(qualifiedName)
          || CommonClassNames.JAVA_UTIL_COLLECTION.equals(qualifiedName);
      if (isCollection) {
        return true;
      }
    }
    return false;
  }
}
