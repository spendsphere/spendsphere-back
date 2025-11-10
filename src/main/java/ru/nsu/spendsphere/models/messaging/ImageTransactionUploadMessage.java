package ru.nsu.spendsphere.models.messaging;

import java.util.List;

public record ImageTransactionUploadMessage(
    Long accountId,
    String filename,
    String contentType,
    byte[] data,
    List<CategoryShortMessage> categories) {}
