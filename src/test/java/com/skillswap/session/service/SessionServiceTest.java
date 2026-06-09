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

    @Test
    void bookSession_fail_pastBooking() {
        LocalDateTime pastStart = LocalDateTime.now().minusHours(1);
        LocalDateTime pastEnd = pastStart.plusHours(1);
        SessionBookRequest request = new SessionBookRequest(
                teacherId, "Java", BillingType.FREE, null, null, pastStart, pastEnd
        );

        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        assertThrows(IllegalArgumentException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }

    @Test
    void bookSession_fail_studentConflict() {
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

        when(sessionRepository.findTeacherConflicts(eq(teacherId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        Session conflictingSession = new Session();
        conflictingSession.setStartTime(futureStart);
        conflictingSession.setEndTime(futureEnd);
        when(sessionRepository.findStudentConflicts(eq(studentId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(conflictingSession));

        assertThrows(ScheduleConflictException.class, () ->
                sessionService.bookSession(student.getEmail(), request)
        );
    }

    @Test
    void acceptSession_success() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.PENDING);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.acceptSession(teacher.getEmail(), sessionId);

        assertNotNull(response);
        assertEquals(SessionStatus.ACCEPTED, response.status());
    }

    @Test
    void acceptSession_fail_sessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.acceptSession(teacher.getEmail(), sessionId)
        );
    }

    @Test
    void acceptSession_fail_wrongTeacher() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.PENDING);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.acceptSession("other@test.com", sessionId)
        );
    }

    @Test
    void acceptSession_fail_invalidStatus() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.ACCEPTED); // already accepted

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.acceptSession(teacher.getEmail(), sessionId)
        );
    }

    @Test
    void rejectSession_success() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.PENDING);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.rejectSession(teacher.getEmail(), sessionId);

        assertNotNull(response);
        assertEquals(SessionStatus.REJECTED, response.status());
    }

    @Test
    void rejectSession_fail_sessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.rejectSession(teacher.getEmail(), sessionId)
        );
    }

    @Test
    void rejectSession_fail_wrongTeacher() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.PENDING);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.rejectSession("other@test.com", sessionId)
        );
    }

    @Test
    void rejectSession_fail_invalidStatus() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.REJECTED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.rejectSession(teacher.getEmail(), sessionId)
        );
    }

    @Test
    void completeSession_success() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.ACCEPTED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepository.countCompletedByUserId(teacherId)).thenReturn(5);
        when(sessionRepository.countCompletedByUserId(studentId)).thenReturn(3);

        SessionResponse response = sessionService.completeSession(teacher.getEmail(), sessionId);

        assertNotNull(response);
        assertEquals(SessionStatus.COMPLETED, response.status());
        verify(userRepository, times(1)).save(teacher);
        verify(userRepository, times(1)).save(student);
        assertEquals(5, teacher.getTotalSessions());
        assertEquals(3, student.getTotalSessions());
    }

    @Test
    void completeSession_fail_sessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.completeSession(teacher.getEmail(), sessionId)
        );
    }

    @Test
    void completeSession_fail_wrongUser() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.ACCEPTED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.completeSession("external@test.com", sessionId)
        );
    }

    @Test
    void completeSession_fail_invalidStatus() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.PENDING); // not accepted

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.completeSession(teacher.getEmail(), sessionId)
        );
    }

    @Test
    void cancelSession_success() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.ACCEPTED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessionResponse response = sessionService.cancelSession(student.getEmail(), sessionId, "Sick");

        assertNotNull(response);
        assertEquals(SessionStatus.CANCELLED, response.status());
        assertEquals("Sick", response.cancelReason());
    }

    @Test
    void cancelSession_fail_sessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.cancelSession(student.getEmail(), sessionId, "Reason")
        );
    }

    @Test
    void cancelSession_fail_wrongUser() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.ACCEPTED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.cancelSession("other@test.com", sessionId, "Reason")
        );
    }

    @Test
    void cancelSession_fail_invalidStatus() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.COMPLETED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(InvalidOperationException.class, () ->
                sessionService.cancelSession(student.getEmail(), sessionId, "Reason")
        );
    }

    @Test
    void getMySessions_success_withStatus() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        Session s = new Session();
        s.setId(UUID.randomUUID());
        s.setTeacher(teacher);
        s.setStudent(student);
        s.setStatus(SessionStatus.COMPLETED);
        when(sessionRepository.findByUserIdAndStatus(studentId, SessionStatus.COMPLETED)).thenReturn(List.of(s));

        List<SessionResponse> results = sessionService.getMySessions(student.getEmail(), SessionStatus.COMPLETED);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(SessionStatus.COMPLETED, results.get(0).status());
    }

    @Test
    void getMySessions_success_all() {
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        Session s = new Session();
        s.setId(UUID.randomUUID());
        s.setTeacher(teacher);
        s.setStudent(student);
        s.setStatus(SessionStatus.PENDING);
        when(sessionRepository.findByUserId(studentId)).thenReturn(List.of(s));

        List<SessionResponse> results = sessionService.getMySessions(student.getEmail(), null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(SessionStatus.PENDING, results.get(0).status());
    }

    @Test
    void getMySessions_fail_userNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.getMySessions("unknown@test.com", null)
        );
    }

    @Test
    void getSession_success() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setStatus(SessionStatus.PENDING);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        SessionResponse response = sessionService.getSession(sessionId);

        assertNotNull(response);
        assertEquals(sessionId, response.id());
    }

    @Test
    void getSession_notFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                sessionService.getSession(sessionId)
        );
    }
}
