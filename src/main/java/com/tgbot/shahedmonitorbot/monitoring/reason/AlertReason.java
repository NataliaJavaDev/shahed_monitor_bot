package com.tgbot.shahedmonitorbot.monitoring.reason;

import java.util.List;

public record AlertReason(

    List<AlertReasonItem> items
) {
}