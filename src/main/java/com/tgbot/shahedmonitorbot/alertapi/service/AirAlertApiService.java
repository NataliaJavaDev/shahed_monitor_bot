package com.tgbot.shahedmonitorbot.alertapi.service;

import com.tgbot.shahedmonitorbot.alertapi.client.AirAlertApiClient;
import com.tgbot.shahedmonitorbot.alertapi.dto.*;
import com.tgbot.shahedmonitorbot.alertapi.model.*;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.AlertType;
import com.tgbot.shahedmonitorbot.monitoring.AlertLifecycleService;
import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class AirAlertApiService {

    private static final Logger log = LoggerFactory.getLogger(AirAlertApiService.class);
    private static final ZoneId KYIV_ZONE = ZoneId.of("Europe/Kyiv");

    // ТИМЧАСОВО: дублювати ключові події перевірки API в debug-чат Telegram.
    private static final boolean DEBUG_TO_TELEGRAM = true;

    private final AirAlertApiClient client;
    private final AppProperties properties;
    private final ManualAlertService manualAlertService;
    private final AlertLifecycleService alertLifecycleService;
    private final AlertDeliveryService alertDeliveryService;

    private volatile ApiAlertStatus lastStatus = new ApiAlertStatus(
            DangerLevel.NONE,
            false,
            LocalDateTime.now(KYIV_ZONE)
    );

    /** false, доки не було першого успішного опиту після старту. */
    private boolean initialized = false;

    /** Стан самих опитувань API (успіхи та помилки) для виводу статусу. */
    private volatile ApiCheckHealth health = ApiCheckHealth.initial();

    public AirAlertApiService(
            AirAlertApiClient client,
            AppProperties properties,
            ManualAlertService manualAlertService,
            AlertLifecycleService alertLifecycleService,
            AlertDeliveryService alertDeliveryService
    ) {
        this.client = client;
        this.properties = properties;
        this.manualAlertService = manualAlertService;
        this.alertLifecycleService = alertLifecycleService;
        this.alertDeliveryService = alertDeliveryService;
    }

    public void checkAlerts() {

        try {

            RegionAlertDto[] alerts = client.fetchAlerts();

            if (alerts == null) {
                // Порожня/пошкоджена відповідь - не відбій. Стан не змінюємо.
                log.warn("Alert API returned null body, state left unchanged");
                recordFailure("Порожня відповідь API");
                reportFailure("⚠️ [API] Порожня відповідь, стан не змінено");
                return;
            }

            ApiAlertStatus currentStatus = detectAlertStatus(alerts);
            boolean recovered = health.consecutiveFailures() > 0;
            recordSuccess();

            if (recovered) {
                debugToTelegram("✅ [API] Зв'язок з API відновлено");
            }

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
                debugToTelegram("🔄 [API] Статус змінився:\n"
                        + formatStatusForLog(lastStatus)
                        + "\n->\n"
                        + formatStatusForLog(currentStatus));
            } else {
                log.debug("API alert status unchanged: {}", formatStatusForLog(currentStatus));
            }

            lastStatus = currentStatus;

        } catch (Exception e) {
            log.error("Alert API check failed: {}", e.getMessage(), e);
            recordFailure(describeError(e));
            reportFailure("❌ [API] Помилка перевірки: " + describeError(e));
        }
    }

    /**
     * Перший успішний опит після запуску: лише синхронізуємо стан,
     * без повідомлень у чат (щоб перезапуск не дублював тривогу).
     */
    private void initialize(ApiAlertStatus currentStatus) {

        // ТИМЧАСОВО (для тестування): при старті повторно повідомляємо поточний стан.
        // Спочатку фіксуємо стан, щоб збій відправки не призводив до повторів на кожному опиті.
        lastStatus = currentStatus;
        initialized = true;

        if (currentStatus.districtAlertActive()) {
            alertLifecycleService.enableMonitoringFromApi();
            manualAlertService.sendApiAlert(
                    currentStatus.districtLevel() == DangerLevel.RED
                            ? AlertType.ALERT
                            : AlertType.YELLOW_ALERT
            );
        }

        if (currentStatus.highRiskActive()) {
            manualAlertService.sendApiAlert(AlertType.HIGH_RISK);
        }

        log.info("API alert status initialized: {}", formatStatusForLog(currentStatus));
        debugToTelegram("ℹ️ [API] Старт, початковий стан: " + formatStatusForLog(currentStatus));
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

    public ApiCheckHealth getHealth() {
        return health;
    }

    private void recordSuccess() {
        health = health.success(LocalDateTime.now(KYIV_ZONE));
    }

    private void recordFailure(String message) {
        health = health.failure(LocalDateTime.now(KYIV_ZONE), message);
    }

    private void debugToTelegram(String text) {

        if (!DEBUG_TO_TELEGRAM) {
            return;
        }

        try {
            alertDeliveryService.sendDebug(text);
        } catch (Exception e) {
            log.warn("Debug send to Telegram failed: {}", e.getMessage());
        }
    }

    /** Помилку шлемо лише при першій у серії, щоб не спамити кожним опитом. */
    private void reportFailure(String text) {

        if (health.consecutiveFailures() == 1) {
            debugToTelegram(text);
        }
    }

    private String describeError(Exception e) {

        String text = e.getClass().getSimpleName()
                + (e.getMessage() == null ? "" : ": " + e.getMessage());

        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}