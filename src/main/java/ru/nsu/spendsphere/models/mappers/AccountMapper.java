package ru.nsu.spendsphere.models.mappers;

import org.springframework.stereotype.Component;
import ru.nsu.spendsphere.models.dto.AccountDTO;
import ru.nsu.spendsphere.models.entities.Account;

@Component
public class AccountMapper {
  public AccountDTO toAccountDTO(Account account) {
    if (account == null) {
      return null;
    }
    return new AccountDTO(
        account.getId(),
        account.getUser().getId(),
        account.getAccountType(),
        account.getBalance(),
        account.getCurrency(),
        account.getName(),
        account.getIconUrl(),
        account.getCreditLimit(),
        account.getIsActive(),
        account.getIncludeInTotal(),
        account.getCreatedAt(),
        account.getUpdatedAt());
  }
}
