package com.skillswap.session.service;

import com.skillswap.availability.repository.AvailabilityRepository;
import com.skillswap.common.exception.InvalidOperationException;
import com.skillswap.common.exception.ResourceNotFoundException;
import com.skillswap.common.exception.ScheduleConflictException;
import com.skillswap.session.dto.SessionBookRequest;
import com.skillswap.session.dto.SessionResponse;
import com.skillswap.session.entity.Session;
import com.skillswap.session.enums.SessionStatus;
import com.skillswap.session.repository.SessionRepository;
import com.skillswap.user.entity.User;
import com.skillswap.user.repository.UserRepository;
import com.skillswap.skill.enums.SkillDirection;
import com.skillswap.skill.repository.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserSkillRepository userSkillRepository;

    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          AvailabilityRepository availabilityRepository,
                          UserSkillRepository userSkillRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.availabilityRepository = availabilityRepository;
        this.userSkillRepository = userSkillRepository;
    }

    /**
     * Book a session with full conflict detection:
     * 1. Teacher ≠ Student
     * 2. End time > Start time
     * 3. Teacher availability covers the requested slot
     * 4. Teacher teaches the skill
     * 5. Student learns the skill
     * 6. No conflicting sessions for teacher
     * 7. No conflicting sessions for student
     */
    @Transactional
    public SessionResponse bookSession(String studentEmail, SessionBookRequest request) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        User teacher = userRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        // 1. Validate teacher ≠ student
        if (teacher.getId().equals(student.getId())) {
            throw new InvalidOperationException("You cannot book a session with yourself");
        }

        // 2. Validate time range
        if (!request.endTime().isAfter(request.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (request.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book a session in the past");
        }

        // 3. Validate teacher has skill listed with direction = TEACH
        boolean teacherHasSkill = userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                teacher.getId(), request.skillName(), SkillDirection.TEACH);
        if (!teacherHasSkill) {
            throw new InvalidOperationException("Teacher does not teach the requested skill: " + request.skillName());
        }

        // 4. Validate student has skill listed with direction = LEARN
        boolean studentHasSkill = userSkillRepository.existsByUserIdAndSkillNameIgnoreCaseAndDirection(
                student.getId(), request.skillName(), SkillDirection.LEARN);
        if (!studentHasSkill) {
            throw new InvalidOperationException("Student does not learn the requested skill: " + request.skillName());
        }

        // 5. Check teacher availability covers the requested time
        int dayOfWeek = request.startTime().getDayOfWeek().getValue(); // 1=MON to 7=SUN
        var coveringSlots = availabilityRepository.findCoveringSlot(
                teacher.getId(), dayOfWeek,
                request.startTime().toLocalTime(),
                request.endTime().toLocalTime()
        );
        if (coveringSlots.isEmpty()) {
            throw new ScheduleConflictException(
                    "Teacher is not available at the requested time. Check their availability slots.");
        }

        // 6. Check teacher has no conflicting sessions
        var teacherConflicts = sessionRepository.findTeacherConflicts(
                teacher.getId(), request.startTime(), request.endTime());
        if (!teacherConflicts.isEmpty()) {
            throw new ScheduleConflictException(
                    "Teacher has a conflicting session from " +
                    teacherConflicts.get(0).getStartTime() + " to " +
                    teacherConflicts.get(0).getEndTime());
        }

        // 7. Check student has no conflicting sessions
        var studentConflicts = sessionRepository.findStudentConflicts(
                student.getId(), request.startTime(), request.endTime());
        if (!studentConflicts.isEmpty()) {
            throw new ScheduleConflictException(
                    "You have a conflicting session from " +
                    studentConflicts.get(0).getStartTime() + " to " +
                    studentConflicts.get(0).getEndTime());
        }

        // All checks passed — create the session
        Session session = new Session();
        session.setTeacher(teacher);
        session.setStudent(student);
        session.setSkillName(request.skillName());
        session.setBillingType(request.billingType());
        session.setPrice(request.price());
        session.setSwapSessionId(request.swapSessionId());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setStatus(SessionStatus.PENDING);

        session = sessionRepository.save(session);

        // Generate virtual room URL
        session.setVirtualRoomUrl("https://meet.jit.si/skillswap-" + session.getId());
        session = sessionRepository.save(session);

        return toResponse(session);
    }

    @Transactional
    public SessionResponse acceptSession(String teacherEmail, UUID sessionId) {
        Session session = getSessionById(sessionId);
        validateTeacher(teacherEmail, session);
        validateStatus(session, SessionStatus.PENDING, "Only pending sessions can be accepted");

        session.setStatus(SessionStatus.ACCEPTED);
        session = sessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public SessionResponse rejectSession(String teacherEmail, UUID sessionId) {
        Session session = getSessionById(sessionId);
        validateTeacher(teacherEmail, session);
        validateStatus(session, SessionStatus.PENDING, "Only pending sessions can be rejected");

        session.setStatus(SessionStatus.REJECTED);
        session = sessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public SessionResponse completeSession(String email, UUID sessionId) {
        Session session = getSessionById(sessionId);
        validateParticipant(email, session);
        validateStatus(session, SessionStatus.ACCEPTED, "Only accepted sessions can be completed");

        session.setStatus(SessionStatus.COMPLETED);
        session = sessionRepository.save(session);

        // Update total sessions count for both participants
        User teacher = session.getTeacher();
        teacher.setTotalSessions(sessionRepository.countCompletedByUserId(teacher.getId()));
        userRepository.save(teacher);

        User student = session.getStudent();
        student.setTotalSessions(sessionRepository.countCompletedByUserId(student.getId()));
        userRepository.save(student);

        return toResponse(session);
    }

    @Transactional
    public SessionResponse cancelSession(String email, UUID sessionId, String reason) {
        Session session = getSessionById(sessionId);
        validateParticipant(email, session);

        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot cancel a " + session.getStatus().name().toLowerCase() + " session");
        }

        session.setStatus(SessionStatus.CANCELLED);
        session.setCancelReason(reason);
        session = sessionRepository.save(session);
        return toResponse(session);
    }

    public List<SessionResponse> getMySessions(String email, SessionStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Session> sessions;
        if (status != null) {
            sessions = sessionRepository.findByUserIdAndStatus(user.getId(), status);
        } else {
            sessions = sessionRepository.findByUserId(user.getId());
        }
        return sessions.stream().map(this::toResponse).toList();
    }

    public SessionResponse getSession(UUID sessionId) {
        return toResponse(getSessionById(sessionId));
    }

    // ── Helper Methods ──

    private Session getSessionById(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    private void validateTeacher(String email, Session session) {
        if (!session.getTeacher().getEmail().equals(email)) {
            throw new InvalidOperationException("Only the teacher can perform this action");
        }
    }

    private void validateParticipant(String email, Session session) {
        if (!session.getTeacher().getEmail().equals(email) &&
            !session.getStudent().getEmail().equals(email)) {
            throw new InvalidOperationException("Only session participants can perform this action");
        }
    }

    private void validateStatus(Session session, SessionStatus expected, String message) {
        if (session.getStatus() != expected) {
            throw new InvalidOperationException(message);
        }
    }

    private SessionResponse toResponse(Session s) {
        return new SessionResponse(
                s.getId(),
                s.getTeacher().getId(),
                s.getTeacher().getFirstName() + " " + s.getTeacher().getLastName(),
                s.getStudent().getId(),
                s.getStudent().getFirstName() + " " + s.getStudent().getLastName(),
                s.getSkillName(),
                s.getBillingType(),
                s.getPrice(),
                s.getSwapSessionId(),
                s.getStartTime(),
                s.getEndTime(),
                s.getStatus(),
                s.getVirtualRoomUrl(),
                s.getCancelReason(),
                s.getCreatedAt()
        );
    }
}
