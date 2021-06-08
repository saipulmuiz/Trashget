/**
 * Copyright (c) 2015-2021 TomTom N.V. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom N.V. and its subsidiaries and may be used
 * for internal evaluation purposes or commercial use strictly subject to separate licensee
 * agreement between you and TomTom. If you are the licensee, you are only permitted to use
 * this Software in accordance with the terms of your license agreement. If you are not the
 * licensee then you are not authorised to use this software in any manner and should
 * immediately return it to TomTom N.V.
 */
package com.cektrend.trashget.utils;

import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteSettings;
import com.tomtom.online.sdk.map.RouteStyle;

import timber.log.Timber;

public class RouteUtils {

    public static void setRoutesInactive(RouteSettings routeSettings) {
        Timber.v("setRoutesInactive()");
        for (Route route : routeSettings.getRoutes()) {
            setRouteInactive(route.getId(), routeSettings);
        }
    }

    @SuppressWarnings("unused")
    public static void setRoutesActive(RouteSettings routeSettings) {
        Timber.v("setRoutesActive()");
        for (Route route : routeSettings.getRoutes()) {
            setRouteActive(route.getId(), routeSettings);
        }
    }

    public static void setRouteActive(long routeId, RouteSettings routeSettings) {
        Timber.v("setRouteActive(id = " + routeId + ")");
        routeSettings.updateRouteStyle(routeId, RouteStyle.DEFAULT_ROUTE_STYLE);
        routeSettings.bringRouteToFront(routeId);
    }

    public static void setRouteInactive(long routeId, RouteSettings routeSettings) {
        Timber.v("setRouteInactive(id = " + routeId + ")");
        routeSettings.updateRouteStyle(routeId, RouteStyle.DEFAULT_INACTIVE_ROUTE_STYLE);
    }

}
