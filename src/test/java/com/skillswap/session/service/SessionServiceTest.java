package com.skillswap.session.service;

import com.skillswap.availability.entity.Availability;
import com.skillswap.availability.repository.AvailabilityRepository;
import com.skillswap.common.exception.InvalidOperationException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.common.exception.ScheduleConflictException;
import com.skillswap.session.dto.SessionBookRequest;
import com.skillswap.session.dto.SessionResponse;
import com.skillswap.session.entity.Session;
import com.skillswap.session.enums.BillingType;
import com.skillswap.session.enums.SessionStatus;
import com.skillswap.session.repository.SessionRepository;
import com.skillswap.skill.enums.SkillDirection;
import com.skillswap.skill.repository.UserSkillRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @InjectMocks
    private SessionService sessionService;

    private User student;
    private User teacher;
    private UUID studentId;
    private UUID teacherId;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        teacherId = UUID.randomUUID();

        student = new User();
        student.setId(studentId);
        student.setEmail("student@test.com");
        student.setFirstName("John");
        student.setLastName("Doe");

        teacher = new User();
        teacher.setId(teacherId);
        teacher.setEmail("teacher@test.com");
        teacher.setFirstName("Jane");
        teacher.setLastName("Smith");

        futureStart = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        futureEnd = futureStart.plusHours(1);
    }

    @Test
    void bookSession_success() {
        SessionBookRequest request = new SessionBookRequest(
                teacherId, "Java", BillingType.SWAP, null, null, futureStart, futureEnd
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                teacherId, "Java", SkillDirection.TEACH)).thenReturn(true);
        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                studentId, "Java", SkillDirection.LEARN)).thenReturn(true);

        Availability availability = new Availability();
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        when(availabilityRepository.findCoveringSlot(
                eq(teacherId), anyInt(), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(List.of(availability));

        when(sessionRepository.findTeacherConflicts(eq(teacherId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(sessionRepository.findStudentConflicts(eq(studentId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        Session savedSession = new Session();
        savedSession.setId(UUID.randomUUID());
        savedSession.setTeacher(teacher);
        savedSession.setStudent(student);
        savedSession.setSkillName("Java");
        savedSession.setBillingType(BillingType.SWAP);
        savedSession.setStartTime(futureStart);
        savedSession.setEndTime(futureEnd);
        savedSession.setStatus(SessionStatus.PENDING);
        savedSession.setVirtualRoomUrl("https://meet.jit.si/skillswap-test");

        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

        SessionResponse response = sessionService.bookSession(student.getEmail(), request);

        assertNotNull(response);
        assertEquals("Java", response.skillName());
        assertEquals(SessionStatus.PENDING, response.status());
        verify(sessionRepository, times(2)).save(any(Session.class));
    }

    @Test
    void bookSession_fail_selfBooking() {
        SessionBookRequest request = new SessionBookRequest(
                studentId, "Java", BillingType.FREE, null, null, futureStart, futureEnd
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }

    @Test
    void bookSession_fail_invalidTimeRange() {
        SessionBookRequest request = new SessionBookRequest(
                teacherId, "Java", BillingType.FREE, null, null, futureEnd, futureStart
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        assertThrows(IllegalArgumentException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }

    @Test
    void bookSession_fail_teacherMissingSkill() {
        SessionBookRequest request = new SessionBookRequest(
                teacherId, "Java", BillingType.FREE, null, null, futureStart, futureEnd
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                teacherId, "Java", SkillDirection.TEACH)).thenReturn(false);

        assertThrows(InvalidOperationException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }

    @Test
    void bookSession_fail_studentMissingSkill() {
        SessionBookRequest request = new SessionBookRequest(
                teacherId, "Java", BillingType.FREE, null, null, futureStart, futureEnd
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                teacherId, "Java", SkillDirection.TEACH)).thenReturn(true);
        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                studentId, "Java", SkillDirection.LEARN)).thenReturn(false);

        assertThrows(InvalidOperationException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }

    @Test
    void bookSession_fail_teacherNotAvailable() {
        SessionBookRequest request = new SessionBookRequest(
                teacherId, "Java", BillingType.FREE, null, null, futureStart, futureEnd
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                teacherId, "Java", SkillDirection.TEACH)).thenReturn(true);
        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                studentId, "Java", SkillDirection.LEARN)).thenReturn(true);

        when(availabilityRepository.findCoveringSlot(
                eq(teacherId), anyInt(), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(ScheduleConflictException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }

    @Test
    void bookSession_fail_teacherConflict() {
        SessionBookRequest request = new SessionBookRequest(
                teacherId, "Java", BillingType.FREE, null, null, futureStart, futureEnd
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                teacherId, "Java", SkillDirection.TEACH)).thenReturn(true);
        when(userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                studentId, "Java", SkillDirection.LEARN)).thenReturn(true);

        Availability availability = new Availability();
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        when(availabilityRepository.findCoveringSlot(
                eq(teacherId), anyInt(), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(List.of(availability));

        Session conflictingSession = new Session();
        conflictingSession.setStartTime(futureStart);
        conflictingSession.setEndTime(futureEnd);

        when(sessionRepository.findTeacherConflicts(eq(teacherId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(conflictingSession));

        assertThrows(ScheduleConflictException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }
}
