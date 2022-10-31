package com.clarenceho.rbsa;

import org.jeasy.rules.api.Rules;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.reader.YamlRuleDefinitionReader;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class RulesFactory {
  Context context;

  public RulesFactory(Context context) {
    this.context = context;
  }

  public Rules getRules() {
    MVELRuleFactory ruleFactory = new MVELRuleFactory(new YamlRuleDefinitionReader());
    Rules allRules = new Rules();

    try (Stream<Path> stream = Files.list(context.getRulesPath())) {
      stream
          .filter(file -> !Files.isDirectory(file))
          .map(Path::toAbsolutePath)
          .map(Path::toString)
          .forEach(f -> {
            try {
              Rules rules = ruleFactory.createRules(new FileReader(f));
              rules.forEach(allRules::register);
            } catch (Exception e) {
              System.err.println("Problem loading rule " + f);
              e.printStackTrace(System.err);
            }

          });
    } catch (Exception e) {
      System.err.println("Problem finding rules");
      e.printStackTrace(System.err);
    }

    return allRules;
  }
}
