package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import com.tgbot.shahedmonitorbot.alertapi.dto.*;
import com.tgbot.shahedmonitorbot.alertapi.model.*;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.AlertType;
import com.tgbot.shahedmonitorbot.monitoring.AlertLifecycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AirAlertApiService {

    private static final Logger log = LoggerFactory.getLogger(AirAlertApiService.class);
    private static final ZoneId KYIV_ZONE = ZoneId.of("Europe/Kyiv");

    private static final Path DIFF_LOG = Path.of("alerts-diff.log");

    private final AirAlertApiClient client;
    private final AppProperties properties;
    private final ManualAlertService manualAlertService;
    private final AlertLifecycleService alertLifecycleService;

    private volatile ApiAlertStatus lastStatus = new ApiAlertStatus(
        DangerLevel.NONE,
        false,
        LocalDateTime.now(KYIV_ZONE)
    );

    // private ApiAlertStatus lastStatus = new ApiAlertStatus(false,
    //     false,
    //     LocalDateTime.now(KYIV_ZONE));

    /** false, доки не було першого успішного опиту після старту. */
    private boolean initialized = false;

    /** ТИМЧАСОВО: попередній зріз записів для diff-логу. */
    private Map<String, String> lastSnapshot = new HashMap<>();

    public AirAlertApiService(
        AirAlertApiClient client,
        AppProperties properties,
        ManualAlertService manualAlertService,
        AlertLifecycleService alertLifecycleService
    ) {
        this.client = client;
        this.properties = properties;
        this.manualAlertService = manualAlertService;
        this.alertLifecycleService = alertLifecycleService;
    }

    public void checkAlerts() {

        try {

            RegionAlertDto[] alerts = client.fetchAlerts();

            if (alerts == null) {
                // Порожня/пошкоджена відповідь - не відбій. Стан не змінюємо.
                log.warn("Alert API returned null body, state left unchanged");
                return;
            }

            logDiff(alerts);

            ApiAlertStatus currentStatus = detectAlertStatus(alerts);

            if (!initialized) {
                initialize(currentStatus);
                return;
            }

            processDistrictAlertChange(lastStatus, currentStatus);
            processHighRiskChange(lastStatus, currentStatus);

            if (hasStatusChanged(lastStatus, currentStatus)) {
                log.info("API alert status changed: {} -> {}",
                        formatStatusForLog(lastStatus),
                        formatStatusForLog(currentStatus));
            } else {
                log.debug("API alert status unchanged: {}", formatStatusForLog(currentStatus));
            }

            lastStatus = currentStatus;

        } catch (Exception e) {
            log.error("Alert API check failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Перший успішний опит після запуску: лише синхронізуємо стан,
     * без повідомлень у чат (щоб перезапуск не дублював тривогу).
     */
    private void initialize(ApiAlertStatus currentStatus) {

        if (currentStatus.districtAlertActive()) {
            alertLifecycleService.enableMonitoringFromApi();
        }

        lastStatus = currentStatus;
        initialized = true;

        log.info("API alert status initialized: {}", formatStatusForLog(currentStatus));
    }

    private void processDistrictAlertChange(ApiAlertStatus previousStatus, ApiAlertStatus currentStatus) {

        DangerLevel was = previousStatus.districtLevel();
        DangerLevel now = currentStatus.districtLevel();

        if (was == now) {
            return;
        }

        // Єдиний відбій: тривог (ні червоної, ні жовтої) більше немає.
        if (now == DangerLevel.NONE) {
            alertLifecycleService.disableMonitoringFromApi();
            manualAlertService.sendApiAlert(AlertType.ALL_CLEAR);
            return;
        }

        // Моніторинг вмикаємо лише при переході з "немає тривоги".
        // Заміна RED <-> YELLOW моніторинг не чіпає.
        if (was == DangerLevel.NONE) {
            alertLifecycleService.enableMonitoringFromApi();
        }

        manualAlertService.sendApiAlert(
                now == DangerLevel.RED ? AlertType.ALERT : AlertType.YELLOW_ALERT
        );
    }

    private void processHighRiskChange(ApiAlertStatus previousStatus, ApiAlertStatus currentStatus) {

        boolean wasActive = previousStatus.highRiskActive();
        boolean isActive = currentStatus.highRiskActive();

        /*
         * HIGH_RISK надсилається тільки тоді, коли активується громада
         * з high-risk-region-id (711). Окремого відбою немає,
         * на моніторинг не впливає.
         */
        if (!wasActive && isActive) {
            manualAlertService.sendApiAlert(AlertType.HIGH_RISK);
        }
    }

    private ApiAlertStatus detectAlertStatus(RegionAlertDto[] alerts) {

        String districtId = properties.alertApi().alarmRegionId();
        String highRiskId = properties.alertApi().highRiskRegionId();

        DangerLevel level = Arrays.stream(alerts)
                .filter(Objects::nonNull)
                .filter(alert -> districtId.equals(alert.regionId()))
                .flatMap(alert -> extractLevels(alert).stream())
                .max(Comparator.naturalOrder())
                .orElse(DangerLevel.NONE);

        boolean highRiskActive = Arrays.stream(alerts)
                .filter(Objects::nonNull)
                .anyMatch(alert -> highRiskId.equals(alert.regionId()));

        return new ApiAlertStatus(
                level,
                highRiskActive,
                LocalDateTime.now(KYIV_ZONE)
        );
    }

    /**
     * Рівні однієї області/району/громади з усіх її activeAlerts.
     * Якщо API ще не віддає activeAlertLevels (або вони порожні, а регіон
     * є у відповіді) - працюємо як раніше: присутність регіону = RED.
     */
    private List<DangerLevel> extractLevels(RegionAlertDto region) {

        List<DangerLevel> result = new ArrayList<>();

        if (region.activeAlerts() != null) {
            for (ActiveAlertDto activeAlert : region.activeAlerts()) {

                if (activeAlert == null || activeAlert.activeAlertLevels() == null) {
                    continue;
                }

                for (ActiveAlertLevelDto level : activeAlert.activeAlertLevels()) {
                    if (level != null) {
                        result.add(DangerLevel.fromApi(level.alertLevel()));
                    }
                }
            }
        }

        if (result.isEmpty()) {
            result.add(DangerLevel.RED);
        }

        return result;
    }

    private boolean hasStatusChanged(ApiAlertStatus previousStatus, ApiAlertStatus currentStatus) {
        return previousStatus.districtLevel() != currentStatus.districtLevel()
                || previousStatus.highRiskActive() != currentStatus.highRiskActive();
    }

    private String formatStatusForLog(ApiAlertStatus status) {
        return "districtLevel=" + status.districtLevel()
                + ", highRiskActive=" + status.highRiskActive();
    }

    public ApiAlertStatus getLastStatus() {
        return lastStatus;
    }

    // ------------------------------------------------------------------
    // ТИМЧАСОВА ДІАГНОСТИКА: пише в alerts-diff.log лише зміни записів
    // району, громади high-risk та Київської області. Після з'ясування,
    // як приходить підвищена небезпека, цей блок і виклик logDiff(alerts)
    // у checkAlerts() можна видалити.
    // ------------------------------------------------------------------

    private void logDiff(RegionAlertDto[] alerts) {

        try {

            String districtId = properties.alertApi().alarmRegionId();
            String highRiskId = properties.alertApi().highRiskRegionId();

            Map<String, String> now = Arrays.stream(alerts)
                    .filter(Objects::nonNull)
                    .filter(a -> a.regionId() != null)
                    .filter(a -> districtId.equals(a.regionId())
                            || highRiskId.equals(a.regionId())
                            || (a.regionName() != null && a.regionName().contains("Київськ")))
                    .collect(Collectors.toMap(
                            RegionAlertDto::regionId,
                            RegionAlertDto::toString,
                            (x, y) -> y
                    ));

            now.forEach((id, value) -> {
                if (!value.equals(lastSnapshot.get(id))) {
                    diffLog("+ " + value);
                }
            });

            lastSnapshot.keySet().stream()
                    .filter(id -> !now.containsKey(id))
                    .forEach(id -> diffLog("- " + id));

            lastSnapshot = new HashMap<>(now);

        } catch (Exception e) {
            // Діагностика ніколи не повинна ламати основну логіку.
            log.warn("logDiff failed: {}", e.getMessage());
        }
    }

    private void diffLog(String message) {

        try {
            Files.writeString(
                    DIFF_LOG,
                    LocalDateTime.now(KYIV_ZONE) + " " + message + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            log.warn("diff log write failed: {}", e.getMessage());
        }
    }
}