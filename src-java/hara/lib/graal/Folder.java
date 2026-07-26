/*
 * Copyright 2018 Transposit Corporation. All Rights Reserved.
 */

package hara.lib.graal;

public interface Folder {
  public Folder getParent();

  public String getPath();

  public String getFile(String name);

  public Folder getFolder(String name);
}
