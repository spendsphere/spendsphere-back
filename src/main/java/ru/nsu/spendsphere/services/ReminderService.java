package ru.nsu.spendsphere.services;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.ReminderCreateDTO;
import ru.nsu.spendsphere.models.dto.ReminderDTO;
import ru.nsu.spendsphere.models.dto.ReminderUpdateDTO;
import ru.nsu.spendsphere.models.entities.Account;
import ru.nsu.spendsphere.models.entities.RecurrenceType;
import ru.nsu.spendsphere.models.entities.Reminder;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.mappers.ReminderMapper;
import ru.nsu.spendsphere.repositories.AccountRepository;
import ru.nsu.spendsphere.repositories.ReminderRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class ReminderService {

  private final ReminderRepository reminderRepository;
  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final ReminderMapper reminderMapper;

  public List<ReminderDTO> getAll(Long userId) {
    ensureUserExists(userId);
    return reminderRepository.findByUserId(userId).stream()
        .map(reminderMapper::toReminderDTO)
        .collect(Collectors.toList());
  }

  public ReminderDTO getById(Long userId, Long reminderId) {
    ensureUserExists(userId);
    Reminder reminder =
        reminderRepository
            .findById(reminderId)
            .filter(r -> r.getUser().getId() == userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Reminder with id " + reminderId + " not found for user " + userId));
    return reminderMapper.toReminderDTO(reminder);
  }

  @Transactional
  public ReminderDTO create(Long userId, @Valid ReminderCreateDTO dto) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found"));
    Account account = null;
    if (dto.accountId() != null) {
      account =
          accountRepository
              .findByIdAndUserId(dto.accountId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Account with id " + dto.accountId() + " not found for user " + userId));
    }

    validateRecurrence(
        dto.recurrenceType(),
        dto.weeklyDayOfWeek(),
        dto.monthlyDayOfMonth(),
        dto.monthlyUseLastDay());

    Reminder reminder =
        Reminder.builder()
            .user(user)
            .account(account)
            .title(dto.title())
            .description(dto.description())
            .amount(dto.amount())
            .recurrenceType(dto.recurrenceType())
            .weeklyDayOfWeek(dto.weeklyDayOfWeek())
            .monthlyDayOfMonth(dto.monthlyDayOfMonth())
            .monthlyUseLastDay(Boolean.TRUE.equals(dto.monthlyUseLastDay()))
            .isActive(dto.isActive() == null || dto.isActive())
            .build();

    return reminderMapper.toReminderDTO(reminderRepository.save(reminder));
  }

  @Transactional
  public ReminderDTO update(Long userId, Long reminderId, @Valid ReminderUpdateDTO dto) {
    ensureUserExists(userId);
    Reminder reminder =
        reminderRepository
            .findById(reminderId)
            .filter(r -> r.getUser().getId() == userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Reminder with id " + reminderId + " not found for user " + userId));

    if (dto.title() != null) reminder.setTitle(dto.title());
    if (dto.description() != null) reminder.setDescription(dto.description());
    if (dto.amount() != null) reminder.setAmount(dto.amount());
    if (dto.isActive() != null) reminder.setIsActive(dto.isActive());

    if (dto.accountId() != null) {
      Account account =
          accountRepository
              .findByIdAndUserId(dto.accountId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Account with id " + dto.accountId() + " not found for user " + userId));
      reminder.setAccount(account);
    }

    if (dto.recurrenceType() != null) reminder.setRecurrenceType(dto.recurrenceType());
    if (dto.weeklyDayOfWeek() != null) reminder.setWeeklyDayOfWeek(dto.weeklyDayOfWeek());
    if (dto.monthlyDayOfMonth() != null) reminder.setMonthlyDayOfMonth(dto.monthlyDayOfMonth());
    if (dto.monthlyUseLastDay() != null) reminder.setMonthlyUseLastDay(dto.monthlyUseLastDay());

    validateRecurrence(
        reminder.getRecurrenceType(),
        reminder.getWeeklyDayOfWeek(),
        reminder.getMonthlyDayOfMonth(),
        reminder.getMonthlyUseLastDay());

    return reminderMapper.toReminderDTO(reminderRepository.save(reminder));
  }

  @Transactional
  public void delete(Long userId, Long reminderId) {
    ensureUserExists(userId);
    Reminder reminder =
        reminderRepository
            .findById(reminderId)
            .filter(r -> r.getUser().getId() == userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Reminder with id " + reminderId + " not found for user " + userId));
    reminderRepository.delete(reminder);
  }

  public List<ReminderDTO> getUpcoming(Long userId, int days) {
    ensureUserExists(userId);
    LocalDate today = LocalDate.now();
    LocalDate end = today.plusDays(days);
    List<Reminder> reminders = reminderRepository.findActiveByUserId(userId);
    List<Reminder> upcoming = new ArrayList<>();
    for (Reminder r : reminders) {
      if (occursBetween(r, today, end)) {
        upcoming.add(r);
      }
    }
    return upcoming.stream().map(reminderMapper::toReminderDTO).collect(Collectors.toList());
  }

  private boolean occursBetween(Reminder r, LocalDate from, LocalDate to) {
    switch (r.getRecurrenceType()) {
      case DAILY:
        return true;
      case WEEKLY:
        if (r.getWeeklyDayOfWeek() == null) {
          return false;
        }
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
          if (cursor.getDayOfWeek() == r.getWeeklyDayOfWeek()) {
            return true;
          }
          cursor = cursor.plusDays(1);
        }
        return false;
      case MONTHLY:
        Integer dom = r.getMonthlyDayOfMonth();
        boolean useLast = Boolean.TRUE.equals(r.getMonthlyUseLastDay());
        LocalDate m = from;
        while (!m.isAfter(to)) {
          YearMonth ym = YearMonth.from(m);
          int lastDay = ym.lengthOfMonth();
          int targetDay = useLast ? lastDay : dom != null ? Math.min(dom, lastDay) : -1;
          if (targetDay != -1) {
            LocalDate candidate = ym.atDay(targetDay);
            if (!candidate.isBefore(from) && !candidate.isAfter(to)) {
              return true;
            }
          }
          m = m.plusDays(1);
        }
        return false;
      default:
        return false;
    }
  }

  private void validateRecurrence(
      RecurrenceType type, java.time.DayOfWeek weekly, Integer monthlyDom, Boolean monthlyLast) {
    if (type == RecurrenceType.WEEKLY) {
      if (weekly == null) {
        throw new IllegalArgumentException("weeklyDayOfWeek is required for WEEKLY reminders");
      }
    }
    if (type == RecurrenceType.MONTHLY) {
      if (Boolean.TRUE.equals(monthlyLast)) {
        return;
      }
      if (monthlyDom == null || monthlyDom < 1 || monthlyDom > 30) {
        throw new IllegalArgumentException(
            "monthlyDayOfMonth must be 1-30 when monthlyUseLastDay is false");
      }
    }
  }

  private void ensureUserExists(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
  }
}
