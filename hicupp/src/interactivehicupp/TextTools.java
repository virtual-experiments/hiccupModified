package interactivehicupp;

import java.text.*;

public final class TextTools {
  private static final NumberFormat infoNumberFormat = new DecimalFormat("0.000");
  
  /**
   * Returns a nine-character textual representation of the given number, on the
   * condition that the exponent is one digit.
   */
  public static String formatScientific(double number) {
    if (Double.isNaN(number))
      return "      NaN";
    else if (Double.isInfinite(number))
      return number < 0 ? "-Infinity" : "+Infinity";
    else if (number == 0.0)
      return " 0       ";
    else {
      boolean minus = number < 0;
      if (minus)
        number = -number;
      int exp = (int) (Math.floor(Math.log(number) / Math.log(10)));
      double mantissa = number / Math.pow(10, exp);
      return (minus ? "-" : " ") + infoNumberFormat.format(mantissa) + "E" + (exp >= 0 ? "+" : "") + exp;
    }
  }
}
