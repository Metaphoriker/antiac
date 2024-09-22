package de.godcipher.antiac.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class PaginatedList<T> {

  private final List<T> items;
  @Getter private final int pageSize;

  public PaginatedList(int pageSize) {
    this.items = new ArrayList<>();
    this.pageSize = pageSize;
  }

  public void addItem(T item) {
    items.add(item);
  }

  public void addAll(List<T> items) {
    this.items.addAll(items);
  }

  public List<T> getPage(int pageNumber) {
    int fromIndex = (pageNumber - 1) * pageSize;
    int toIndex = Math.min(fromIndex + pageSize, items.size());
    if (fromIndex >= items.size() || fromIndex < 0) {
      return new ArrayList<>();
    }
    return items.subList(fromIndex, toIndex);
  }

  public int getTotalPages() {
    return (int) Math.ceil((double) items.size() / pageSize);
  }

  public int getTotalItems() {
    return items.size();
  }
}
