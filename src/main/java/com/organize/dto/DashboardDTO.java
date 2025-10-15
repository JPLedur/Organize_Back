package com.organize.dto;

import java.util.List;

public record DashboardDTO(
    Double monthlyRevenue,
    Long appointmentsToday,
    Long confirmedAppointmentsToday,
    String nextAppointmentTime,
    String nextAppointmentDescription,
    Long newCustomers,
    List<AppointmentDTO> topUpcomingAppointments,
    List<TopCustomerDTO> topCustomers,
    List<RecentReviewDTO> recentReviews,
    AppointmentDTO nextAppointment,
    Long totalAppointments,
    List<AppointmentDTO> upcomingAppointments
) {}
