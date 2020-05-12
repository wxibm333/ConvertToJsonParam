package com.wxibm333.util;

import com.google.common.collect.Sets;
import com.google.gson.internal.$Gson$Preconditions;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangXin
 * @version v1.0.0
 * @date 2020-05-11 15:23
 */
public class ToolsUtil {

  private final static String PATTERN = "yyyy-MM-dd HH:mm:ss";
  private final static DateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN);
  private final static Set<String> COLLECTION_TYPE = Sets.newHashSet("Set", "List", "Collection");
  private static final Map<String, Object> NORMAL_TYPES = new HashMap<>();

  static {
    NORMAL_TYPES.put("Boolean", false);
    NORMAL_TYPES.put("Byte", 0);
    NORMAL_TYPES.put("Short", (short) 0);
    NORMAL_TYPES.put("Integer", 0);
    NORMAL_TYPES.put("Long", 0L);
    NORMAL_TYPES.put("Float", 0.0F);
    NORMAL_TYPES.put("Double", 0.0D);
    NORMAL_TYPES.put("String", "demoData");
    NORMAL_TYPES.put("BigDecimal", 0.0);
    NORMAL_TYPES.put("Date", DATE_FORMAT.format(new Date()));
    NORMAL_TYPES.put("Timestamp", System.currentTimeMillis());
    NORMAL_TYPES.put("LocalDate", LocalDate.now().toString());
    NORMAL_TYPES.put("LocalTime", LocalTime.now().toString());
    NORMAL_TYPES.put("LocalDateTime", LocalDateTime.now().toString());
  }

  public static boolean isNormalType(String typeName) {
    return NORMAL_TYPES.containsKey(typeName);
  }

  public static boolean isCollection(String fieldTypeName) {
    for (String type : COLLECTION_TYPE) {
      if (fieldTypeName.startsWith(type)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断字段类型是否是原始数据类型以及封装类型
   * @param type 字段类型
   * @return boolean
   * @author wangXin
   * @date 2020-05-12 13:43
   */
  public static boolean isPrimitiveOrNormalType(PsiType type) {
    String fieldTypeName = type.getPresentableText();
    return type instanceof PsiPrimitiveType || ToolsUtil.isNormalType(fieldTypeName);
  }

  /**
   * 通过字段类型，解析类型class对象,如果是集合或者数组类型，则返回泛型类型,基础类型或者普通类型时返回null
   *
   * @param type 字段类型
   * @return com.intellij.psi.PsiClass
   * @author wangXin
   * @date 2020-05-11 16:46
   */
  public static @Nullable PsiClass resolveClassByType(@NotNull PsiType type) {
    if(ToolsUtil.isPrimitiveOrNormalType(type)){
      return null;
    }
    // 提取集合泛型类型.集合以及迭代器都属于PsiClassType，type instanceof PsiClassType = true
    PsiType psiType = PsiUtil.extractIterableTypeParameter(type, false);
    PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
    if(psiClass == null){
      return PsiUtil.resolveClassInType(type);
    }
    return psiClass;
  }
}
