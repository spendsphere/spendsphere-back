package ru.nsu.spendsphere.models.mappers;

import org.springframework.stereotype.Component;
import ru.nsu.spendsphere.models.dto.ReminderDTO;
import ru.nsu.spendsphere.models.entities.Reminder;

@Component
public class ReminderMapper {

  public ReminderDTO toReminderDTO(Reminder reminder) {
    if (reminder == null) return null;
    return new ReminderDTO(
        reminder.getId(),
        reminder.getUser().getId(),
        reminder.getTitle(),
        reminder.getDescription(),
        reminder.getAmount(),
        reminder.getRecurrenceType(),
        reminder.getWeeklyDayOfWeek(),
        reminder.getMonthlyDayOfMonth(),
        reminder.getMonthlyUseLastDay(),
        reminder.getIsActive(),
        reminder.getAccount() != null ? reminder.getAccount().getId() : null,
        reminder.getAccount() != null ? reminder.getAccount().getName() : null,
        reminder.getCreatedAt(),
        reminder.getUpdatedAt());
  }
}
