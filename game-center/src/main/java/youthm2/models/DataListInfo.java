package youthm2.models;

import java.time.LocalDate;
import java.util.List;

public class DataListInfo {
  public String[] fileName = new String[255];
  // THandle
  public String mapFileHandle;
  // PChar
  public String mapFileBuffer;
  // TDateTime
  public LocalDate dateTime;
  // PChar
  public String data;
  public long dataSize;
  // TListItem
  public List<String> item;
}
