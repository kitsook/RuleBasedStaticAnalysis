import java.math.BigDecimal;

/**
 * A sample to test Java parser and rules to detech calling BigDecimal toString() explicitly / implicitly
 */
public class Sample1 {
  public void callExplicitly() {
    BigDecimal bd = new BigDecimal("123.4");
    System.out.println(bd.toString());
  }

  public void callImplicitly() {
    BigDecimal bd = new BigDecimal("987.65");
    System.out.println("" + bd);
    System.out.println(bd + "");
    System.out.println("" + "123" + bd + "");
  }

  public void notCalling() {
    BigDecimal bd = new BigDecimal("42");
    bd.add(new BigDecimal("1337"));
  }
}