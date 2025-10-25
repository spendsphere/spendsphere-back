package ru.nsu.spendsphere.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.ReminderCreateDTO;
import ru.nsu.spendsphere.models.dto.ReminderDTO;
import ru.nsu.spendsphere.models.dto.ReminderUpdateDTO;
import ru.nsu.spendsphere.models.entities.Account;
import ru.nsu.spendsphere.models.entities.AccountType;
import ru.nsu.spendsphere.models.entities.RecurrenceType;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.repositories.AccountRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ReminderServiceIntegrationTest {

  @Autowired private ReminderService reminderService;
  @Autowired private UserRepository userRepository;
  @Autowired private AccountRepository accountRepository;

  @Test
  void createReadUpdateDeleteFlow() {
    User user = createUser();
    Account account = createAccount(user, "Основная карта");

    ReminderDTO created =
        reminderService.create(
            user.getId(),
            new ReminderCreateDTO(
                "Оплата интернета",
                "Тариф Домашний",
                new BigDecimal("600.00"),
                RecurrenceType.MONTHLY,
                null,
                20,
                false,
                true,
                account.getId()));

    assertNotNull(created.id());
    assertEquals("Оплата интернета", created.title());
    assertEquals(RecurrenceType.MONTHLY, created.recurrenceType());
    assertEquals(20, created.monthlyDayOfMonth());

    ReminderDTO byId = reminderService.getById(user.getId(), created.id());
    assertEquals(created.id(), byId.id());

    ReminderDTO updated =
        reminderService.update(
            user.getId(),
            created.id(),
            new ReminderUpdateDTO(
                "Оплата интернета (обновлено)",
                null,
                new BigDecimal("650.00"),
                RecurrenceType.WEEKLY,
                DayOfWeek.MONDAY,
                null,
                null,
                true,
                account.getId()));

    assertEquals("Оплата интернета (обновлено)", updated.title());
    assertEquals(new BigDecimal("650.00"), updated.amount());
    assertEquals(RecurrenceType.WEEKLY, updated.recurrenceType());
    assertEquals(DayOfWeek.MONDAY, updated.weeklyDayOfWeek());

    reminderService.delete(user.getId(), created.id());
    assertThrows(
        ResourceNotFoundException.class,
        () -> reminderService.getById(user.getId(), created.id()));
  }

  @Test
  void upcomingDailyWeeklyMonthly() {
    User user = createUser();
    Account account = createAccount(user, "Основная");

    reminderService.create(
        user.getId(),
        new ReminderCreateDTO(
            "Ежедневно",
            null,
            new BigDecimal("10.00"),
            RecurrenceType.DAILY,
            null,
            null,
            null,
            true,
            account.getId()));

    reminderService.create(
        user.getId(),
        new ReminderCreateDTO(
            "Еженедельно",
            null,
            new BigDecimal("20.00"),
            RecurrenceType.WEEKLY,
            LocalDate.now().getDayOfWeek(),
            null,
            null,
            true,
            account.getId()));

    int dom = Math.min(30, LocalDate.now().getDayOfMonth());
    reminderService.create(
        user.getId(),
        new ReminderCreateDTO(
            "Ежемесячно",
            null,
            new BigDecimal("30.00"),
            RecurrenceType.MONTHLY,
            null,
            dom,
            false,
            true,
            account.getId()));

    List<ReminderDTO> upcoming = reminderService.getUpcoming(user.getId(), 5);
    assertEquals(3, upcoming.size());
  }

  @Test
  void monthlyLastDayHandlesShortMonths() {
    User user = createUser();
    Account account = createAccount(user, "Основная");

    reminderService.create(
        user.getId(),
        new ReminderCreateDTO(
            "Последний день",
            null,
            new BigDecimal("99.99"),
            RecurrenceType.MONTHLY,
            null,
            null,
            true,
            true,
            account.getId()));

    List<ReminderDTO> upcoming = reminderService.getUpcoming(user.getId(), 31);
    assertNotNull(upcoming);
  }

  private User createUser() {
    User user =
        User.builder().email("user@test.com").password("pass").name("Test").surname("User").build();
    return userRepository.save(user);
  }

  private Account createAccount(User user, String name) {
    Account account =
        Account.builder().user(user).accountType(AccountType.CARD).name(name).build();
    return accountRepository.save(account);
  }
}


