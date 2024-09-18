package de.godcipher.antiac.click;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class CPS {

  public static final CPS EMPTY = new CPS();

  private final LinkedList<Click> clickSet = new LinkedList<>();

  public void addClick(Click click) {
    clickSet.add(click);
  }

  public Click getLastClick() {
    return clickSet.stream().max(Comparator.comparingLong(Click::getTime)).orElse(Click.EMPTY);
  }

  public int getCPS() {
    return clickSet.size();
  }

  public double getAverageDelay() {
    if (clickSet.size() < 2) return 0.0;

    long totalDelay = 0;
    for (int i = 1; i < clickSet.size(); i++) {
      totalDelay += clickSet.get(i).getTime() - clickSet.get(i - 1).getTime();
    }
    return (double) totalDelay / (clickSet.size() - 1);
  }

  public boolean isEmpty() {
    return clickSet.isEmpty();
  }

  public List<Click> getClicks() {
    return new ArrayList<>(clickSet);
  }

  @Override
  public String toString() {
    return String.valueOf(getCPS());
  }
}
