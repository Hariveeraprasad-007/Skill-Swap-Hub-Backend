package com.skillswap.availability.dto;

import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityResponse(
        UUID id, int dayOfWeek, LocalTime startTime, LocalTime endTime, String timezone
) {}
