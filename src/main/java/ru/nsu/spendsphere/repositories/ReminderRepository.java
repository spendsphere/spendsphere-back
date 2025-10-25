package ru.nsu.spendsphere.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Reminder;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

  List<Reminder> findByUserId(Long userId);

  @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.isActive = true")
  List<Reminder> findActiveByUserId(@Param("userId") Long userId);
}
