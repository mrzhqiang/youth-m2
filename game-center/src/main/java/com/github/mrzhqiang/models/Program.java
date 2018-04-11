package com.github.mrzhqiang.models;

public class Program {
  public boolean getStart;
  public boolean reStart;
  // 0,1,2,3 默认，正在启动，已启动，正在关闭
  public int startStatus;
  public String[] programFile = new String[50];
  public String[] directory = new String[100];
  // TProcessInformation
  // TODO 进程信息？
  public String processInfo;
  // THandle
  // TODO 进程处理？
  public String processHandle;
  // THandle
  // TODO 主窗口处理？
  public String mainFormHandle;
  // 窗口坐标
  public int mainFormX;
  public int mainFormY;
}
