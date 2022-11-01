# Rule Based Static Analysis

**WORK IN PROGRESS**

An experiment to combine a rule engine with a Java parser for static analysis.

Wanted to see how easy (or difficult) it is to use rules to scan Java source code for quality / security checks.

Uses [JavaParser](https://github.com/javaparser/javaparser) to create the AST. And for the rule engine, it is using the [easy-rules](https://github.com/j-easy/easy-rules) (sadly the library is in maintenance mode and no further development being done).

Rules can be dfined in YAML files and are used to define both conditions and actions when doing analysis, e.g.
```
---
name: "BigDecimal explicit toString"
description: "find code that explicitly call BigDecimal toString"
condition: "resolvedMethodRef.getPackageName().toString().equals(\"java.math\") &&
  resolvedMethodRef.getClassName().toString().equals(\"BigDecimal\") &&
  node.getName().toString() == \"toString\""
actions:
  - "System.out.print(\"Cautions! Explicitly calling BigDecimal toString() in \" + file.toString());
    if (node.getRange().isPresent()) {
      System.out.println(\" at \" + node.getRange().get().toString());
    }"
```

