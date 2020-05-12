package com.wxibm333.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangXin
 * @version v1.0.0
 * @date 2020-05-09 9:09
 */
public class JavadocForJsonUtil {

  private final static String VALID_PACKAGE = "javax.validation.constraints";

  /**
   * 提取注释对象的描述信息
   *
   * @param docComment javadoc对象
   * @return java.lang.String
   * @author wangXin
   * @date 2020-05-09 14:38
   */
  public static String extractDescriptionComment(@Nullable PsiDocComment docComment) {
    if (docComment == null) {
      return "";
    }
    // 获取描述注解信息，包括换行符，空格等空白字符等
    PsiElement[] descriptionElements = docComment.getDescriptionElements();
    StringBuilder commentInfo = new StringBuilder();
    for (PsiElement descriptionElement : descriptionElements) {
      String text = descriptionElement.getText();
      if (StringUtils.isNotBlank(text)) {
        commentInfo.append(text.trim());
      }
    }
    return commentInfo.toString();
  }

  /**
   * 提取字段的验证信息
   *
   * @param field 字段对象
   * @return com.google.gson.JsonArray
   * @author wangXin
   * @date 2020-05-09 14:38
   */
  public static JsonArray extractValidComment(PsiField field) {
    // 获取字段的验证信息
    PsiAnnotation[] annotations = field.getAnnotations();
    JsonArray validJsonArray = new JsonArray();
    for (PsiAnnotation annotation : annotations) {
      String qualifiedName = annotation.getQualifiedName();
      if (qualifiedName != null && qualifiedName.startsWith(VALID_PACKAGE)) {
        String text = annotation.getText();
        validJsonArray.add(text.substring(1));
      }
    }
    return validJsonArray;
  }

  /**
   * 如果字段是枚举类型，则提取枚举字段信息
   *
   * @param psiClass 字段类型class对象
   * @return com.google.gson.JsonArray
   * @author wangXin
   * @date 2020-05-09 14:39
   */
  public static JsonObject extractEnumComment(PsiClass psiClass) {
    // 提取枚举参数信息
    boolean isEnumType = psiClass != null && psiClass.isEnum();
    if (isEnumType) {
      JsonObject enumJsonObject = new JsonObject();
      PsiField[] enumFields = psiClass.getFields();
      for (PsiField enumField : enumFields) {
        if (enumField instanceof PsiEnumConstant) {
          enumJsonObject.addProperty(enumField.getName(),
              JavadocForJsonUtil.extractDescriptionComment(enumField.getDocComment()));
        }
      }
      return enumJsonObject;
    }
    return null;
  }

  /**
   * 处理集合字段信息
   *
   * @param psiClass 引用类型
   * @return com.google.gson.JsonObject
   * @author wangXin
   * @date 2020-05-09 14:40
   */
  public static JsonObject generateReferenceComment(@Nullable PsiClass psiClass) {
    JsonObject jsonObject = new JsonObject();
    if (psiClass != null) {
      PsiField[] allFields = psiClass.getAllFields();
      for (PsiField field : allFields) {
        jsonObject.add(field.getName(), JavadocForJsonUtil.generateDocComment(field));
      }
      return jsonObject;
    }
    return null;
  }

  /**
   * JsonElement对象添加到目标对象
   *
   * @param isAppend 是否添加到目标类
   * @param key      key值
   * @param value    JsonElement子类value值
   * @param target   目标对象
   * @author wangXin
   * @date 2020-05-09 14:40
   */
  public static void appendToJsonObject(Boolean isAppend, String key, JsonElement value,
      JsonObject target) {
    if (isAppend) {
      target.add(key, value);
    }

  }

  /**
   * 生成该字段注释对象信息,类似如下信息
   * <pre>
   *   {
   *   "equipmentInfoList": {
   *     "comment": "异常设备",
   *     "type": "List<RenderEquipmentInfo>",
   *     "valid": [],
   *     "deepType": {
   *       "type": "RenderEquipmentInfo",
   *       "comment": {
   *         "name": {
   *           "comment": "设备名称",
   *           "type": "String",
   *           "valid": [
   *             "NotBlank"
   *           ]
   *         },
   *         "status": {
   *           "comment": "设备状态",
   *           "type": "String",
   *           "valid": [
   *             "NotBlank"
   *           ]
   *         }
   *       }
   *     }
   *   }
   * }
   * </pre>
   *
   * @param field 字段对象
   * @return com.google.gson.JsonObject
   * @author wangXin
   * @date 2020-05-09 9:15
   */
  public static JsonObject generateDocComment(@NotNull PsiField field) {

    JsonObject commentJsonObject = new JsonObject();
    PsiDocComment docComment = field.getDocComment();
    String descriptionComment = JavadocForJsonUtil.extractDescriptionComment(docComment);
    // 获取字段类型
    PsiType type = field.getType();
    String fieldTypeName = type.getPresentableText();
    JsonArray validComment = JavadocForJsonUtil.extractValidComment(field);
    // 判断字段类型是否是原始数据类型以及封装类型
    boolean isPrimitiveType = ToolsUtil.isPrimitiveOrNormalType(type);
    commentJsonObject.addProperty("comment", descriptionComment);
    if (isPrimitiveType) {
      // 基础数据类型，加上验证注释信息
      commentJsonObject.add("valid", validComment);
    } else {
      PsiClass psiClass = ToolsUtil.resolveClassByType(type);
      boolean isEnum = psiClass != null && psiClass.isEnum();
      commentJsonObject.addProperty("type", isEnum ? "enum" : fieldTypeName);
      if (isEnum) {
        //枚举类处理
        JsonObject jsonObject = JavadocForJsonUtil.extractEnumComment(psiClass);
        JavadocForJsonUtil
            .appendToJsonObject(jsonObject != null, "optionalValue", jsonObject, commentJsonObject);
      } else {
        // psiClass可能是集合或数组中嵌套的基础数据类型,如为原始类型的封装类型无需处理
        boolean normalType = psiClass != null && ToolsUtil.isNormalType(psiClass.getName());
        if(psiClass != null && !normalType){
          JsonObject collectionDocComment = JavadocForJsonUtil.generateReferenceComment(psiClass);
          JavadocForJsonUtil
              .appendToJsonObject(collectionDocComment != null, psiClass.getName(),
                  collectionDocComment,
                  commentJsonObject);
        }
      }
    }
    return commentJsonObject;
  }

}
