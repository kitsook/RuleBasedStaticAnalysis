package com.clarenceho.rbsa;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import org.checkerframework.checker.units.qual.A;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Runner {
  private static Instrumentation inst = null;

  Context context;
  RulesFactory rulesFactory;
  SourceRoot sourceRoot;
  RulesEngine rulesEngine;
  Rules rules;

  public Runner(Context context, RulesFactory rulesFactory) throws Exception {
    this.context = context;
    this.rulesFactory = rulesFactory;

    ParserConfiguration parserConfiguration = new ParserConfiguration()
        .setSymbolResolver(new JavaSymbolSolver(new ReflectionTypeSolver()));

    this.sourceRoot = new SourceRoot(context.getSourcesPath(), parserConfiguration);
    this.rulesEngine = new DefaultRulesEngine();

    this.rules = this.rulesFactory.getRules();
    // TODO find ways to load code being analyzed to we can use reflection to get details
    //updateClassLoader(this.context.getSourcesPath());
  }

  public void run() {
    try (Stream<Path> stream = Files.list(context.getSourcesPath())) {
      stream
          .filter(file -> !Files.isDirectory(file))
          .map(Path::getFileName)
          .map(Path::toString)
          .forEach(f -> {

            CompilationUnit cu = sourceRoot.parse("", f);
            cu.accept(new GenericVisitorAdapter<Node, A>() {
              // TODO factory to map AST details to facts. how to handle all diff nodes???

              @Override
              public Node visit(MethodCallExpr n, A arg) {
                ResolvedMethodDeclaration resolvedMethodRef = n.resolve();
                // prepare the facts
                Facts facts = new Facts();
                facts.put("file", f);
                facts.put("node", (Node)n);
                facts.put("methodCallExpr", n);
                facts.put("resolvedMethodRef", resolvedMethodRef);
                rulesEngine.fire(rules, facts);

                return super.visit(n, arg);
              }

              @Override
              public Node visit(BinaryExpr be, A arg) {
                ResolvedType resolvedType = be.calculateResolvedType();
                // prepare the facts
                Facts facts = new Facts();
                facts.put("file", f);
                facts.put("node", (Node)be);
                facts.put("binaryExpr", be);
                facts.put("left", be.getLeft());
                facts.put("right", be.getRight());
                facts.put("resolvedType", resolvedType);

                rulesEngine.fire(rules, facts);

                return super.visit(be, arg);
              }
            }, null);

          });

    } catch (Exception e) {
      System.err.println("Problem running static analysis");
      e.printStackTrace(System.err);
    }
  }

  protected void updateClassLoader(Path path) throws Exception {
    ClassLoader cl = ClassLoader.getSystemClassLoader();

    try {
      // java 9 or higher
      if (!(cl instanceof URLClassLoader)) {
        inst.appendToSystemClassLoaderSearch(new JarFile(path.toFile()));
        return;
      }

      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(cl, (Object)path.toUri().toURL());
    } catch (Throwable t) {
      t.printStackTrace();
      throw new Exception("Cannot add URL " + path + " to system classloader");
    }

  }

  // called before main. using maven-assembly-plugin to set agentmain
  public static void agentmain(final String a, final Instrumentation inst) {
    Runner.inst = inst;
  }

  public static void main(String[] args) throws Exception {
    Context context = new Context(Paths.get("src/main/resources/rules"), Paths.get("src/main/resources/samples"));
    RulesFactory rulesFactory = new RulesFactory(context);
    Runner runner = new Runner(context, rulesFactory);
    runner.run();
  }
}
