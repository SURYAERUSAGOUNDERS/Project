package com.exchange.book;

import java.util.NoSuchElementException;

public class PriceLevel<T extends com.exchange.model.Order> {

    public static class Node<E> {
        public E value;
        public Node<E> prev;
        public Node<E> next;

        public Node(E value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size = 0;

    public Node<T> addLast(T value) {
        Node<T> node = new Node<T>(value);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
        return node;
    }
    

    public T removeFirst() {
        if (head == null) throw new NoSuchElementException("PriceLevel empty");
        Node<T> n = head;
        T val = n.value;

        head = n.next;
        if (head != null) head.prev = null;
        else tail = null;

        size--;
        return val;
    }

    public void remove(Node<T> n) {
        if (n == null) return;

        Node<T> p = n.prev;
        Node<T> nx = n.next;

        if (p != null) p.next = nx;
        else head = nx;

        if (nx != null) nx.prev = p;
        else tail = p;

        size--;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node<T> firstNode() {
        return head;
    }

    public int size() {
        return size;
    }

    public long totalQuantity() {
        long sum = 0;
        Node<T> n = head;
        while (n != null) {
            sum += n.value.getRemaining();
            n = n.next;
        }
        return sum;
    }

    public T peekFirst() {
        if (head == null) throw new NoSuchElementException();
        return head.value;
    }
}
