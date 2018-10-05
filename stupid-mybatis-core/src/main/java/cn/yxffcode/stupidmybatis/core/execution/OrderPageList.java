package cn.yxffcode.stupidmybatis.core.execution;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

/**
 * @author gaohang
 */
public class OrderPageList<E, ID> extends AbstractList<E> implements Serializable {

  private static final OrderPageList<Object, Object> EMPTY_LIST = new OrderPageList<>(null, Collections.emptyList());

  public static <T> OrderPageList emptyList() {
    return EMPTY_LIST;
  }

  /**
   * 一页中最后的元素的id
   */
  private ID lastId;

  private final List<E> elements = Lists.newArrayList();

  public OrderPageList() {
  }

  public OrderPageList(ID lastId, List<E> elements) {
    this.lastId = lastId;
    this.elements.addAll(elements);
  }

  @Override
  public E get(int index) {
    return elements.get(index);
  }

  @Override
  public boolean add(E e) {
    return elements.add(e);
  }

  @Override
  public E remove(int index) {
    return elements.remove(index);
  }

  public ID getLastId() {
    return lastId;
  }

  public void setLastId(ID lastId) {
    this.lastId = lastId;
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public String toString() {
    return "OrderPageList{" +
        "lastId=" + lastId +
        ", elements=" + elements +
        '}';
  }
}
