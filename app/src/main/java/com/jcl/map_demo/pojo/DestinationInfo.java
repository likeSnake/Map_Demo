package com.jcl.map_demo.pojo;

import com.mapbox.geojson.Point;

public class DestinationInfo {
    private String name;
    private String address;
    private String distance;
    private Point point;

    public DestinationInfo(String name, String address, String distance, Point point) {
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
