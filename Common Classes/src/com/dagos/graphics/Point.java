package com.dagos.graphics;

/**
 * Created by Dmitry on 05.03.14.
 */

public class Point {
    public int x;
    public int y;
    public int sliceId;

    public Point() {

    }

    public Point(int x, int y, int sliceId) {
        this.x = x;
        this.y = y;
        this.sliceId = sliceId;
    }

    public boolean equals(Object object) {
        if (object != null) {
            if (this.getClass() == object.getClass()) {
                Point point = (Point) object;

                if (this.x == point.x && this.y == point.y && this.sliceId == point.sliceId) {
                    return true;
                }
            }
        }
        return false;
    }
}