package com.clarenceho.rbsa;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class Context {
  Path rulesPath;
  Path sourcesPath;

  public Context(Path rulesPath, Path sourcesPath) {
    this.rulesPath = rulesPath;
    this.sourcesPath = sourcesPath;
  }
}
