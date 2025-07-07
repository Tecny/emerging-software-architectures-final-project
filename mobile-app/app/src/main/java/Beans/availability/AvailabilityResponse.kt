package Beans.availability

data class AvailabilityResponse(
    val weeklyAvailability: Map<String, List<String>>
)