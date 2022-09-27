package com.francosoft.kampalacleantoilets.utilities.helpers

object Constants {

    const val SEARCH_BY_KAMPALA = 1
    const val SEARCH_BY_DIVISION = 2
    const val APP_SHARED_PREFERENCES = "app_sp"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_GEO_PREF = "fences_on"
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_ITEM"
    const val CHANNEL_ID = "Nearby Toilet"
    const val NOTIFICATION_ID = 33
    const val SHOWCASE_ID = "maps_showcase"
    const val REPORT_TYPE = "report"
    const val All_TOILETS_REPORT = "All_Toilets_Report"
    const val DIVISIONS_REPORT = "Divisions_Report"
    const val STATUS_REPORT = "Status_Report"
    const val TYPE_REPORT = "Type_Report"

    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 3 // random unique value
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 4
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 5

    const val GEO_TAG = "GEOFENCE_TAG"
    const val EXTRA_NEAR_TOILET = "com.francosoft.kampalacleantoilets.ui.map.MapsFragment.NEAR_BY_TOILET"
    const val ACTION_GEOFENCE_EVENT =
        "com.francosoft.kampalacleantoilets.ui.map.action.ACTION_GEOFENCE_EVENT"
}