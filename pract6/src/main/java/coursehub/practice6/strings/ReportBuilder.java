package coursehub.practice6.strings;

import java.util.List;

// сборка большого текста: + в цикле создаёт новый String на каждой итерации
public final class ReportBuilder {
  private ReportBuilder() {
  }

  // на больших объёмах плохо: каждый + копирует всё накопленное
  public static String withPlus(List<String> lines) {
    String report = "";
    for (String line : lines) {
      report += line + "\n";
    }
    return report;
  }
}
