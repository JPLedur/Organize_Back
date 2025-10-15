package com.organize.service;

import com.organize.dto.*;
import com.organize.model.Appointment;
import com.organize.model.AppointmentStatus;
import com.organize.repository.AppointmentRepository;
import com.organize.repository.UserRepository;
import com.organize.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public DashboardService(AppointmentRepository appointmentRepository,
                            UserRepository userRepository,
                            ReviewRepository reviewRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    // ✅ Dashboard geral (estabelecimento)
    public DashboardDTO getDashboardData() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(23, 59, 59);

        double monthlyRevenue = appointmentRepository.findByStartTimeBetween(
                hoje.withDayOfMonth(1).atStartOfDay(),
                hoje.withDayOfMonth(hoje.lengthOfMonth()).atTime(23, 59, 59)
        ).stream()
         .mapToDouble(a -> a.getService().getPriceCents() / 100.0)
         .sum();

        long appointmentsToday = appointmentRepository.countByStartTimeBetween(inicioHoje, fimHoje);
        long confirmedAppointmentsToday = appointmentRepository.countByStartTimeBetweenAndStatus(
                inicioHoje, fimHoje, AppointmentStatus.CONFIRMED
        );

        Appointment nextAppointment = appointmentRepository
                .findFirstByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now())
                .orElse(null);

        String nextAppointmentTime = nextAppointment != null ? nextAppointment.getStartTime().toString() : null;
        String nextAppointmentDescription = nextAppointment != null
                ? nextAppointment.getClient().getName() + " - " + nextAppointment.getService().getName()
                : null;

        long newCustomers = userRepository.countByCreatedAtAfter(
                hoje.withDayOfMonth(1).atStartOfDay()
        );

        List<AppointmentDTO> topUpcomingAppointments = appointmentRepository.findByStartTimeAfter(LocalDateTime.now())
                .stream()
                .sorted(Comparator.comparing(Appointment::getStartTime))
                .limit(5)
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        List<TopCustomerDTO> topCustomers = appointmentRepository.findByStartTimeBetween(
                hoje.withDayOfMonth(1).atStartOfDay(),
                hoje.withDayOfMonth(hoje.lengthOfMonth()).atTime(23, 59, 59)
        ).stream()
        .collect(Collectors.groupingBy(Appointment::getClient,
                Collectors.summingDouble(a -> a.getService().getPriceCents() / 100.0)))
        .entrySet().stream()
        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
        .limit(3)
        .map(entry -> new TopCustomerDTO(
                entry.getKey().getName(),
                Math.round(entry.getValue()),
                (int) appointmentRepository.findByClient(entry.getKey()).size()
        ))
        .collect(Collectors.toList());

        List<RecentReviewDTO> recentReviews = reviewRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(r -> new RecentReviewDTO(
                        r.getClient().getName(),
                        r.getRating(),
                        r.getComment()
                ))
                .collect(Collectors.toList());

        // Retorna o DTO completo (campos do cliente ficam nulos)
        return new DashboardDTO(
                monthlyRevenue,
                appointmentsToday,
                confirmedAppointmentsToday,
                nextAppointmentTime,
                nextAppointmentDescription,
                newCustomers,
                topUpcomingAppointments,
                topCustomers,
                recentReviews,
                null, // nextAppointment (cliente)
                null, // totalAppointments (cliente)
                null  // upcomingAppointments (cliente)
        );
    }

    // ✅ Dashboard específico do cliente (usando o mesmo DTO)
    public DashboardDTO getDashboardDataForClient(UUID clientId) {
        LocalDateTime now = LocalDateTime.now();

        Appointment nextAppointment = appointmentRepository
                .findTopByClientIdAndStartTimeAfterAndStatusOrderByStartTimeAsc(clientId, now, AppointmentStatus.CONFIRMED)
                .orElse(null);

        long totalAppointments = appointmentRepository.countByClientId(clientId);

        List<AppointmentDTO> upcomingAppointments = appointmentRepository
                .findByClientIdAndStartTimeAfterOrderByStartTimeAsc(clientId, now)
                .stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());

        // Campos gerais ficam nulos
        return new DashboardDTO(
                null, null, null, null, null, null,
                null, null, null,
                nextAppointment != null ? new AppointmentDTO(nextAppointment) : null,
                totalAppointments,
                upcomingAppointments
        );
    }
}
