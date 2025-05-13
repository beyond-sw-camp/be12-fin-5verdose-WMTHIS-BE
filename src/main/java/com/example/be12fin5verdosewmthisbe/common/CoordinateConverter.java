package com.example.be12fin5verdosewmthisbe.common;

import org.locationtech.proj4j.*;

public class CoordinateConverter {

    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransform transform;

    static {
        CoordinateReferenceSystem epsg5179 = crsFactory.createFromName("EPSG:5174"); // 서울시 좌표계
        CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326"); // 위경도
        transform = new CoordinateTransformFactory().createTransform(epsg5179, epsg4326);
    }

    public static double[] convertToLatLon(double x, double y) {
        ProjCoordinate src = new ProjCoordinate(x, y);
        ProjCoordinate dest = new ProjCoordinate();
        transform.transform(src, dest);
        return new double[]{dest.y, dest.x}; // 위도, 경도
    }
}
