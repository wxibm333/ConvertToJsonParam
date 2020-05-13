package com.wxibm333.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * java bean convert to json param
 *
 * @author wangXin
 * @version v1.0.0
 * @date 2020-04-30 10:15
 */
public class ConvertToJsonParamUtil {

  private final static NotificationGroup NOTIFICATION_GROUP;
  private final static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
      .create();

  static {
    NOTIFICATION_GROUP = new NotificationGroup("JavaBean2JsonParam.NotificationGroup",
        NotificationDisplayType.BALLOON, true);
  }

  // private static boolean isNormalType(String typeName) {
  //   return NORMAL_TYPES.containsKey(typeName);
  // }

  public static void convertToJsonParam(@NotNull AnActionEvent anActionEvent, boolean isShowComment,
      boolean ignore) {
    Editor editor = anActionEvent.getDataContext().getData(CommonDataKeys.EDITOR);
    PsiFile psiFile = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
    Project project = Objects.requireNonNull(editor).getProject();
    PsiElement referenceAt = Objects.requireNonNull(psiFile)
        .findElementAt(editor.getCaretModel().getOffset());
    PsiClass selectedClass = PsiTreeUtil.getContextOfType(referenceAt, PsiClass.class);
    try {
      PsiClass psiClass = Objects.requireNonNull(selectedClass);
      JsonObject jsonObject = convertJsonObject(selectedClass, isShowComment, ignore);
      String json = GSON.toJson(jsonObject);
      StringSelection selection = new StringSelection(json);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(selection, selection);
      String message = String
          .format("Convert %s to JSON success, copied to clipboard.", psiClass.getName());
      Notification success = NOTIFICATION_GROUP
          .createNotification(message, NotificationType.INFORMATION);
      Notifications.Bus.notify(success, project);
    } catch (Exception ex) {
      Notification error = NOTIFICATION_GROUP
          .createNotification("Convert to JSON failed.", NotificationType.ERROR);
      Notifications.Bus.notify(error, project);
    }
  }

  private static void putForReferenceType(@NotNull PsiClass resolveClass, @NotNull String key,
      boolean ignore, @NotNull JsonObject targetJsonObject) {
    if (resolveClass.isEnum()) {
      JsonArray jsonArray = new JsonArray();
      PsiField[] enumFields = resolveClass.getAllFields();
      for (PsiField enumField : enumFields) {
        if (enumField instanceof PsiEnumConstant) {
          jsonArray.add(enumField.getName());
        }
      }
      targetJsonObject.add(key, jsonArray);
    } else if (ToolsUtil.isNormalType(resolveClass)) {
      JsonArray jsonArray = ToolsUtil
          .getDefaultValueForPrimitiveOrNormalType(resolveClass);
      targetJsonObject.add(key, jsonArray);
    } else {
      JsonArray jsonArray = new JsonArray();
      JsonObject convertJsonObject = ConvertToJsonParamUtil
          .convertJsonObject(resolveClass, false, ignore);
      jsonArray.add(convertJsonObject);
      targetJsonObject.add(key, jsonArray);
    }
  }

  public static JsonObject convertJsonObject(PsiClass psiClass, boolean isShowComment,
      boolean ignore) {
    JsonObject jsonObject = new JsonObject();
    JsonObject commentJsonObject = new JsonObject();
    if (psiClass != null) {
      for (PsiField field : psiClass.getAllFields()) {
        PsiType type = field.getType();
        String name = field.getName();

        // doc comment
        if (isShowComment) {
          JsonObject generateDocComment = JavadocForJsonUtil.generateDocComment(field);
          commentJsonObject.add(name, generateDocComment);
        }

        // Generate json parameters
        boolean primitiveOrNormalType = ToolsUtil.isPrimitiveOrNormalType(type);

        if (primitiveOrNormalType) {
          ToolsUtil.putDefaultValueForPrimitiveOrNormalType(type, name, jsonObject);
        } else {
          PsiClass resolveClass = PsiUtil.resolveClassInType(type);
          if (resolveClass != null) {
            if (type instanceof PsiArrayType) {
              // 数组处理
              ConvertToJsonParamUtil.putForReferenceType(resolveClass, name, ignore, jsonObject);
            } else if (resolveClass.isEnum()) {
              // 枚举处理
              PsiField[] enumFields = resolveClass.getAllFields();
              if (enumFields.length > 0) {
                jsonObject.addProperty(name, enumFields[1].getName());
              }
            } else if (ToolsUtil.isCollectionType(resolveClass)) {
              // 集合类型处理
              PsiType psiType = PsiUtil.extractIterableTypeParameter(type, false);
              PsiClass collectionClass = PsiUtil.resolveClassInType(psiType);
              if (collectionClass != null) {
                ConvertToJsonParamUtil
                    .putForReferenceType(collectionClass, name, ignore, jsonObject);
              }
            } else if(!ToolsUtil.isNormalType(resolveClass)){
              jsonObject.add(name, ConvertToJsonParamUtil
                  .convertJsonObject(resolveClass, false, ignore));
            }
          }
        }
      }
      if (isShowComment) {
        jsonObject.add("@comment", commentJsonObject);
      }
    }
    return jsonObject;
  }
}
