package ru.nsu.spendsphere.models.messaging;

import java.util.List;

public record ParsedTransactionsMessage(Long accountId, List<ParsedTransactionItem> transactions) {}
