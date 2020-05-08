package com.wxibm333;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.wxibm333.util.ConvertToJsonParamUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 携带注释转换json参数
 * @author wangXin
 * @version v1.0.0
 * @date 2020-04-29 17:12
 */
public class ConvertToJsonParamCarryDocCommentAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    ConvertToJsonParamUtil.convertToJsonParam(anActionEvent,true,false);
  }
}
