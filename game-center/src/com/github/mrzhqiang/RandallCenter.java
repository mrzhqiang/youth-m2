package com.github.mrzhqiang;

import javax.swing.*;

public class RandallCenter {
  public JPanel root;
  private JButton startBegin;
  private JButton 下一步Button1;
  private JTextArea a1111TextArea;
  private JTextField dMirServerTextField;
  private JTextField heroDBTextField;
  private JTextField 兰达尔TextField;
  private JTextField a127001TextField;
  private JTextField a0TextField;
  private JButton 保存Button;
  private JButton 默认Button;
  private JCheckBox 关闭服务器五行功能CheckBox;
  private JTextField textField1;
  private JTextField textField2;
  private JButton 选择目录Button;
  private JButton 选择目录Button1;
  private JRadioButton 每天RadioButton;
  private JRadioButton 每隔RadioButton;
  private JSpinner spinner1;
  private JSpinner spinner2;
  private JCheckBox 备份CheckBox;
  private JCheckBox 压缩CheckBox;
  private JCheckBox 自动运行备份系统CheckBox;
  private JButton 修改Button;
  private JButton 删除Button;
  private JButton 增加Button;
  private JButton 保存Button1;
  private JButton 启动Button;
  private JSpinner spinner3;
  private JSpinner spinner4;
  private JCheckBox 人物数据CheckBox;
  private JCheckBox NPC打造数据CheckBox;
  private JCheckBox EMail数据CheckBox;
  private JCheckBox 帐号数据CheckBox;
  private JCheckBox 行会数据CheckBox;
  private JCheckBox 沙巴克数据CheckBox;
  private JCheckBox 全局变量CheckBox;
  private JCheckBox 物品ID计数CheckBox;
  private JCheckBox 帐号日志记录CheckBox;
  private JCheckBox 引擎日志记录CheckBox;
  private JCheckBox 游戏日志记录CheckBox;
  private JCheckBox 出师离婚断交数据CheckBox;
  private JButton 开始清理Button;
  private JList list1;

  public RandallCenter() {
    startBegin.addActionListener(e -> {
      HintDialog dialog = new HintDialog();
      dialog.pack();
      dialog.setVisible(true);
    });
  }
}
