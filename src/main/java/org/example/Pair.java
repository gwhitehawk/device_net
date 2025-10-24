package org.example;

public class Pair {
    public String user;
    public int count;

    public Pair(String user, int count) {
        this.user = user;
        this.count = count;
    }

    @Override
    public String toString() {
        return "(" + user + ", " + count + ")";
    }
}
