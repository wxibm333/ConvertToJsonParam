package com.wxibm333.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTreeUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NonNls;
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
  private final static String PATTERN = "yyyy-MM-dd HH:mm:ss";
  private final static DateFormat DATE_FORMAT = new SimpleDateFormat(PATTERN);
  private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @NonNls
  private static final Map<String, Object> NORMAL_TYPES = new HashMap<>();

  static {
    NOTIFICATION_GROUP = new NotificationGroup("JavaBean2JsonParam.NotificationGroup",
        NotificationDisplayType.BALLOON, true);

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
      String json = gson.toJson(jsonObject);
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

  public static JsonObject convertJsonObject(PsiClass psiClass, boolean isShowComment,
      boolean ignore) {
    JsonObject jsonObject = new JsonObject();
    JsonObject commentJsonObject = new JsonObject();
    if (psiClass != null) {
      for (PsiField field : psiClass.getAllFields()) {
        PsiType type = field.getType();
        String name = field.getName();

        // doc comment
        if (field.getDocComment() != null && field.getDocComment().getText() != null) {
          // 获取描述注解信息，包括换行符，空格等空白字符等
          PsiElement[] descriptionElements = field.getDocComment()
              .getDescriptionElements();
          commentJsonObject.addProperty(name, field.getDocComment().getText());
        }
      }
    }
    return commentJsonObject;
  }
}
