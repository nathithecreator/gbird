package com.example.gbird;

import java.util.List;

public class DirectionsResponse {
    public List<Route> routes;

    public class Route {
        public OverviewPolyline overview_polyline;
    }

    public class OverviewPolyline {
        public String points;
    }
}

