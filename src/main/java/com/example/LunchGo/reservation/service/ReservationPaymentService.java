package com.example.LunchGo.reservation.service;

import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.OwnerRepository;
import com.example.LunchGo.member.repository.UserRepository;
import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.dto.*;
import com.example.LunchGo.reservation.entity.Payment;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationCancellation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.mapper.ReservationSummaryMapper;
import com.example.LunchGo.reservation.mapper.row.ReservationMenuItemRow;
import com.example.LunchGo.reservation.repository.PaymentRepository;
import com.example.LunchGo.reservation.repository.ReservationCancellationRepository;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.stats.RestaurantStatsEventService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.LunchGo.sms.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationPaymentService {
    private static final String PAYMENT_STATUS_READY = "READY";
    private static final String PAYMENT_STATUS_REQUESTED = "REQUESTED";
    private static final String PAYMENT_STATUS_PAID = "PAID";
    private static final String PAYMENT_STATUS_FAILED = "FAILED";
    private static final String PAYMENT_METHOD_CARD = "CARD";
    private static final String CARD_TYPE_UNKNOWN = "UNKNOWN";
    private static final String PG_PROVIDER_PORTONE = "PORTONE";

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantRepository restaurantRepository;
    private final PortoneVerificationService portoneVerificationService;
    private final RestaurantStatsEventService statsEventService;
    private final ReservationSummaryMapper reservationSummaryMapper;
    private final ReservationCancellationRepository reservationCancellationRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final PortoneCancelService portoneCancelService;


    @Transactional
    public CreatePaymentResponse createPayment(Long reservationId, CreatePaymentRequest request) {
        Reservation reservation = getReservation(reservationId);
        Integer expectedAmount = resolveExpectedAmount(reservation, request.getPaymentType());

        if (expectedAmount == null || !expectedAmount.equals(request.getAmount())) {
            throw new IllegalArgumentException("결제 금액이 예약 정보와 일치하지 않습니다.");
        }

        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Payment> idempotentPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (idempotentPayment.isPresent()) {
                Payment payment = idempotentPayment.get();
                if (!payment.getReservationId().equals(reservationId)
                    || !payment.getPaymentType().equals(request.getPaymentType())
                    || !payment.getAmount().equals(request.getAmount())) {
                    throw new IllegalArgumentException("이미 처리된 결제 요청입니다.");
                }
                return toCreateResponse(payment);
            }
        }

        Optional<Payment> existing = paymentRepository.findByReservationIdAndPaymentType(
            reservationId,
            request.getPaymentType()
        );
        if (existing.isPresent()) {
            Payment payment = existing.get();
            return toCreateResponse(payment);
        }

        String merchantUid = "RSV-" + reservationId + "-" + System.currentTimeMillis();
        Payment payment = Payment.builder()
            .reservationId(reservationId)
            .paymentType(request.getPaymentType())
            .status(PAYMENT_STATUS_READY)
            .method(resolveMethod(request.getMethod()))
            .cardType(CARD_TYPE_UNKNOWN)
            .amount(request.getAmount())
            .currency(reservation.getCurrency())
            .pgProvider(PG_PROVIDER_PORTONE)
            .merchantUid(merchantUid)
            .idempotencyKey(request.getIdempotencyKey())
            .requestedAt(LocalDateTime.now())
            .build();

        Payment saved = paymentRepository.save(payment);
        return toCreateResponse(saved);
    }

    @Transactional
    public void markPaymentRequested(String merchantUid) {
        if (merchantUid == null || merchantUid.isBlank()) {
            log.warn("결제 요청 시작 기록 실패: merchantUid 누락");
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Optional<Payment> paymentOptional = paymentRepository.findByMerchantUid(merchantUid);
        if (paymentOptional.isEmpty()) {
            log.warn("결제 요청 시작 기록 실패: 결제 정보 없음 (merchantUid={})", merchantUid);
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }
        Payment payment = paymentOptional.get();

        if (PAYMENT_STATUS_READY.equals(payment.getStatus())) {
            payment.setStatus(PAYMENT_STATUS_REQUESTED);
            payment.setRequestedAt(LocalDateTime.now());
        } else {
            log.warn("결제 요청 시작 기록 스킵: 상태={} (merchantUid={})", payment.getStatus(), merchantUid);
        }
    }

    @Transactional
    public void completePayment(PortoneCompleteRequest request) {
        Payment payment = paymentRepository.findByMerchantUid(request.getMerchantUid())
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (PAYMENT_STATUS_PAID.equals(payment.getStatus())) {
            return;
        }

        if (!payment.getAmount().equals(request.getPaidAmount())) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        PortoneVerificationService.PortonePaymentInfo info = portoneVerificationService.verifyPayment(
            payment.getMerchantUid(),
            request.getPaidAmount()
        );

        payment.setStatus(PAYMENT_STATUS_PAID);
        payment.setImpUid(request.getImpUid());
        if (info.getPgTid() != null) {
            payment.setPgTid(info.getPgTid());
        }
        if (info.getMethod() != null) {
            payment.setMethod(normalizeMethod(info.getMethod()));
        }
        if (info.getCardType() != null) {
            payment.setCardType(normalizeCardType(info.getCardType()));
        }
        payment.setApprovedAt(LocalDateTime.now());

        Reservation reservation = getReservation(payment.getReservationId());
        reservation.setStatus(resolveReservationStatus(payment.getPaymentType()));

        recordConfirmAfterCommit(payment, reservation);
        sendOwnerNotificationAfterCommit(reservation);
    }

    @Transactional
    public void failPayment(PortoneFailRequest request) {
        paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(request.getReservationId())
            .ifPresent(payment -> {
                payment.setStatus(PAYMENT_STATUS_FAILED);
                payment.setFailedAt(LocalDateTime.now());
            });
    }

    @Transactional
    public void expirePayment(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        if (!ReservationStatus.TEMPORARY.equals(reservation.getStatus())) {
            log.info("결제 만료 스킵: status={} (reservationId={})", reservation.getStatus(), reservationId);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = resolvePaymentDeadline(reservation);
        if (deadline != null && now.isBefore(deadline)) {
            log.info("결제 만료 스킵: deadline 미도달 (reservationId={}, deadline={}, now={})",
                reservationId, deadline, now);
            return;
        }
        reservation.setStatus(ReservationStatus.EXPIRED);
        log.info("결제 만료 처리: reservationId={}, deadline={}, now={}", reservationId, deadline, now);

        paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
            .ifPresent(payment -> {
                payment.setStatus(PAYMENT_STATUS_FAILED);
                payment.setFailedAt(LocalDateTime.now());
            });
    }

    @Transactional
    public void handleWebhookPaid(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Payment payment = paymentRepository.findByMerchantUid(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (PAYMENT_STATUS_PAID.equals(payment.getStatus())) {
            return;
        }

        PortoneVerificationService.PortonePaymentInfo info = portoneVerificationService.verifyPayment(
            payment.getMerchantUid(),
            payment.getAmount()
        );

        payment.setStatus(PAYMENT_STATUS_PAID);
        payment.setApprovedAt(LocalDateTime.now());
        if (info.getPgTid() != null) {
            payment.setPgTid(info.getPgTid());
        }
        if (info.getMethod() != null) {
            payment.setMethod(normalizeMethod(info.getMethod()));
        }
        if (info.getCardType() != null) {
            payment.setCardType(normalizeCardType(info.getCardType()));
        }

        Reservation reservation = getReservation(payment.getReservationId());
        reservation.setStatus(resolveReservationStatus(payment.getPaymentType()));

        recordConfirmAfterCommit(payment, reservation);
        sendOwnerNotificationAfterCommit(reservation);
    }

    @Transactional
    public void handleWebhookFailed(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Payment payment = paymentRepository.findByMerchantUid(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.setStatus(PAYMENT_STATUS_FAILED);
        payment.setFailedAt(LocalDateTime.now());

        Reservation reservation = getReservation(payment.getReservationId());
        if (ReservationStatus.TEMPORARY.equals(reservation.getStatus())) {
            if (!isPastPaymentDeadline(reservation)) {
                log.info("웹훅 실패: 결제 마감 전이므로 TEMPORARY 유지 (reservationId={})",
                    reservation.getReservationId());
                return;
            }
            reservation.setStatus(ReservationStatus.EXPIRED);
            log.info("웹훅 실패 만료 처리: reservationId={}", reservation.getReservationId());
        }
    }

    @Transactional
    public void handleWebhookCancelled(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다.");
        }

        Payment payment = paymentRepository.findByMerchantUid(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        Reservation reservation = getReservationForUpdate(payment.getReservationId());

        payment.setStatus("CANCELLED");
        payment.setCancelledAt(LocalDateTime.now());

        if (ReservationStatus.TEMPORARY.equals(reservation.getStatus()) && !isPastPaymentDeadline(reservation)) {
            log.info("웹훅 취소: 결제 마감 전이므로 TEMPORARY 유지 (reservationId={})",
                reservation.getReservationId());
            return;
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        log.info("웹훅 취소 처리: reservationId={}", reservation.getReservationId());
    }

    @Transactional(readOnly = true)
    public ReservationConfirmationResponse getConfirmation(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        ReservationSlot slot = getSlot(reservation.getSlotId());
        Restaurant restaurant = getRestaurant(slot.getRestaurantId());
        ReservationCancellation cancellation = reservationCancellationRepository
            .findByReservationId(reservationId)
            .orElse(null);

        Payment payment = paymentRepository.findTopByReservationIdAndStatusOrderByApprovedAtDesc(
                reservationId,
                PAYMENT_STATUS_PAID
            )
            .orElseGet(() -> paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
                .orElse(null));

        List<ReservationMenuItemRow> menuRows = reservationSummaryMapper.selectReservationMenuItems(reservationId);
        List<ReservationConfirmationResponse.MenuItem> menuItems = new ArrayList<>();

        if (menuRows != null && !menuRows.isEmpty()) {
            for (ReservationMenuItemRow row : menuRows) {
                menuItems.add(ReservationConfirmationResponse.MenuItem.builder()
                        .name(row.getName())
                        .quantity(row.getQuantity())
                        .unitPrice(row.getUnitPrice())
                        .lineAmount(row.getLineAmount())
                        .build());
            }
        }

        return ReservationConfirmationResponse.builder()
            .reservationCode(reservation.getReservationCode())
            .status(reservation.getStatus() != null ? reservation.getStatus().name() : null)
            .cancelledBy(cancellation != null ? cancellation.getCancelledBy() : null)
            .restaurant(ReservationConfirmationResponse.RestaurantInfo.builder()
                .name(restaurant.getName())
                .address(formatAddress(restaurant))
                .phone(restaurant.getPhone())
                .build())
            .booking(ReservationConfirmationResponse.BookingInfo.builder()
                .date(slot.getSlotDate().toString())
                .time(slot.getSlotTime().toString())
                .partySize(reservation.getPartySize())
                .requestNote(reservation.getRequestMessage())
                .build())
            .payment(ReservationConfirmationResponse.PaymentInfo.builder()
                .amount(payment != null ? payment.getAmount() : 0)
                .method(formatMethod(payment != null ? payment.getMethod() : null))
                .paidAt(formatPaidAt(payment != null ? payment.getApprovedAt() : null))
                .build())
            .menuItems(menuItems.isEmpty() ? null : menuItems)
            .build();
    }

    @Transactional(readOnly = true)
    public ReservationPaymentStatusResponse getConfirmationStatus(Long reservationId) {
        getReservation(reservationId);
        Payment payment = paymentRepository.findTopByReservationIdAndStatusOrderByApprovedAtDesc(
                reservationId,
                PAYMENT_STATUS_PAID
            )
            .orElse(null);
        boolean paid = payment != null && payment.getApprovedAt() != null;

        return ReservationPaymentStatusResponse.builder()
            .paid(paid)
            .paidAt(formatPaidAt(payment != null ? payment.getApprovedAt() : null))
            .build();
    }

    @Transactional(readOnly = true)
    public ReservationSummaryResponse getSummary(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        ReservationSlot slot = getSlot(reservation.getSlotId());
        Restaurant restaurant = getRestaurant(slot.getRestaurantId());

        Payment payment = paymentRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
            .orElse(null);

        List<ReservationMenuItemRow> menuRows = reservationSummaryMapper.selectReservationMenuItems(reservationId);
        List<ReservationSummaryResponse.MenuItem> menuItems = new ArrayList<>();
        Integer menuTotal = null;
        if (menuRows != null && !menuRows.isEmpty()) {
            int sum = 0;
            for (ReservationMenuItemRow row : menuRows) {
                Integer lineAmount = row.getLineAmount();
                if (lineAmount != null) {
                    sum += lineAmount;
                }
                menuItems.add(ReservationSummaryResponse.MenuItem.builder()
                    .name(row.getName())
                    .quantity(row.getQuantity())
                    .unitPrice(row.getUnitPrice())
                    .lineAmount(row.getLineAmount())
                    .build());
            }
            menuTotal = sum;
        }

        Integer visitCount = reservationSummaryMapper.selectVisitCount(
            restaurant.getRestaurantId(),
            reservation.getUserId()
        );

        Integer totalAmount = menuTotal != null && menuTotal > 0
            ? menuTotal
            : reservation.getTotalAmount();
        if (totalAmount == null) {
            totalAmount = payment != null ? payment.getAmount() : 0;
        }
        LocalDateTime deadline = reservation.getPaymentDeadlineAt();
        if (deadline == null) {
            deadline = reservation.getHoldExpiresAt();
        }
        String holdExpiresAt = formatDeadline(reservation.getHoldExpiresAt());

        return ReservationSummaryResponse.builder()
            .restaurant(ReservationSummaryResponse.RestaurantInfo.builder()
                .name(restaurant.getName())
                .address(formatAddress(restaurant))
                .build())
            .booking(ReservationSummaryResponse.BookingInfo.builder()
                .date(slot.getSlotDate().toString())
                .time(slot.getSlotTime().toString())
                .partySize(reservation.getPartySize())
                .requestNote(reservation.getRequestMessage())
                .build())
            .payment(ReservationSummaryResponse.PaymentInfo.builder()
                .type(payment != null ? payment.getPaymentType() : null)
                .amount(payment != null ? payment.getAmount() : 0)
                .build())
            .requestNote(reservation.getRequestMessage())
            .totalAmount(totalAmount)
            .visitCount(visitCount)
            .paymentDeadlineAt(formatDeadline(deadline))
            .holdExpiresAt(holdExpiresAt)
            .menuItems(menuItems.isEmpty() ? null : menuItems)
            .build();
    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
    }

    private Reservation getReservationForUpdate(Long reservationId) {
        return reservationRepository.findByIdForUpdate(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
    }

    private ReservationSlot getSlot(Long slotId) {
        return reservationSlotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("예약 슬롯 정보를 찾을 수 없습니다."));
    }

    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new IllegalArgumentException("식당 정보를 찾을 수 없습니다."));
    }

    private Integer resolveExpectedAmount(Reservation reservation, String paymentType) {
        if ("DEPOSIT".equals(paymentType)) {
            return reservation.getDepositAmount();
        }
        if ("PREPAID_FOOD".equals(paymentType)) {
            return reservation.getPrepayAmount();
        }
        return null;
    }

    private ReservationStatus resolveReservationStatus(String paymentType) {
        if ("DEPOSIT".equals(paymentType)) {
            return ReservationStatus.CONFIRMED;
        }
        if ("PREPAID_FOOD".equals(paymentType)) {
            return ReservationStatus.PREPAID_CONFIRMED;
        }
        return ReservationStatus.CONFIRMED;
    }

    private String resolveMethod(String method) {
        if (method == null || method.isBlank()) {
            return PAYMENT_METHOD_CARD;
        }
        return method;
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return PAYMENT_METHOD_CARD;
        }
        String normalized = method.trim().toUpperCase();
        if (normalized.contains("CARD")) {
            return "CARD";
        }
        if (normalized.contains("TRANSFER") || normalized.contains("BANK")) {
            return "TRANSFER";
        }
        if (normalized.contains("VIRTUAL")) {
            return "VIRTUAL_ACCOUNT";
        }
        return "UNKNOWN";
    }

    private String normalizeCardType(String cardType) {
        if (cardType == null || cardType.isBlank()) {
            return CARD_TYPE_UNKNOWN;
        }
        String normalized = cardType.trim().toUpperCase();
        if (normalized.contains("CORPORATE") || normalized.contains("BUSINESS")) {
            return "CORPORATE";
        }
        if (normalized.contains("PERSONAL") || normalized.contains("PRIVATE")) {
            return "PERSONAL";
        }
        return CARD_TYPE_UNKNOWN;
    }

    private String formatAddress(Restaurant restaurant) {
        String detail = restaurant.getDetailAddress();
        if (detail == null || detail.isBlank()) {
            return restaurant.getRoadAddress();
        }
        return restaurant.getRoadAddress() + " " + detail;
    }

    private String formatMethod(String method) {
        if (method == null) {
            return "신용카드";
        }
        if ("CARD".equalsIgnoreCase(method)) {
            return "신용카드";
        }
        if ("TRANSFER".equalsIgnoreCase(method)) {
            return "계좌이체";
        }
        return method;
    }

    private String formatPaidAt(LocalDateTime approvedAt) {
        if (approvedAt == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm");
        return approvedAt.format(formatter);
    }

    private String formatDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            return null;
        }
        return deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private void sendOwnerNotificationAfterCommit(Reservation reservation) {
        if (reservation == null) {
            return;
        }
        ReservationSlot slot = getSlot(reservation.getSlotId());
        Restaurant restaurant = getRestaurant(slot.getRestaurantId());
        Owner owner = ownerRepository.findByOwnerId(restaurant.getOwnerId()).orElse(null);
        User user = userRepository.findByUserId(reservation.getUserId()).orElse(null);
        if (owner == null || user == null || owner.getPhone() == null || owner.getPhone().isBlank()) {
            return;
        }

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            smsService.sendNotificationToOwner(owner.getPhone(), OwnerReservationNotification.builder()
                .reservationCode(reservation.getReservationCode())
                .date(slot.getSlotDate().toString())
                .time(slot.getSlotTime().toString())
                .partySize(reservation.getPartySize())
                .name(user.getName())
                .build());
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                smsService.sendNotificationToOwner(owner.getPhone(), OwnerReservationNotification.builder()
                    .reservationCode(reservation.getReservationCode())
                    .date(slot.getSlotDate().toString())
                    .time(slot.getSlotTime().toString())
                    .partySize(reservation.getPartySize())
                    .name(user.getName())
                    .build());
            }
        });
    }

    private void recordConfirmAfterCommit(Payment payment, Reservation reservation) {
        if (payment == null || reservation == null) {
            return;
        }
        Long paymentId = payment.getPaymentId();
        if (paymentId == null) {
            return;
        }
        ReservationSlot slot = getSlot(reservation.getSlotId());
        Long restaurantId = slot.getRestaurantId();
        String dedupeKey = paymentId.toString();

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            statsEventService.recordConfirm(restaurantId, dedupeKey);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                statsEventService.recordConfirm(restaurantId, dedupeKey);
            }
        });
    }

    private CreatePaymentResponse toCreateResponse(Payment payment) {
        return CreatePaymentResponse.builder()
            .paymentId(payment.getPaymentId())
            .merchantUid(payment.getMerchantUid())
            .amount(payment.getAmount())
            .pgProvider(payment.getPgProvider())
            .currency(payment.getCurrency())
            .build();
    }

    private boolean isPastPaymentDeadline(Reservation reservation) {
        if (reservation == null) {
            return false;
        }
        LocalDateTime deadline = resolvePaymentDeadline(reservation);
        if (deadline == null) {
            return false;
        }
        return !LocalDateTime.now().isBefore(deadline);
    }

    private LocalDateTime resolvePaymentDeadline(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        LocalDateTime deadline = reservation.getPaymentDeadlineAt();
        if (deadline == null) {
            deadline = reservation.getHoldExpiresAt();
        }
        return deadline;
    }

    @Transactional
    public int refundDepositByPolicy(Long reservationId, String reason) {
        Reservation reservation = getReservation(reservationId);

        Payment payment = paymentRepository.findByReservationIdAndPaymentType(reservationId, "DEPOSIT")
                .filter(p -> PAYMENT_STATUS_PAID.equals(p.getStatus()))
                .orElseThrow(() -> new IllegalStateException("환불할 예약금 결제(PAID) 정보를 찾을 수 없습니다."));

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("예약 슬롯 정보를 찾을 수 없습니다."));

        int paidAmount = payment.getAmount() != null ? payment.getAmount() : 0;
        int partySize = reservation.getPartySize() != null ? reservation.getPartySize() : 0;

        LocalDateTime reservationDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getSlotTime());
        LocalDateTime now = LocalDateTime.now();

        int refundPercent = computeRefundPercent(now, reservationDateTime, slot.getSlotDate(), partySize, reservation.getStatus());
        int refundAmount = (int) Math.floor(paidAmount * (refundPercent / 100.0));

        reservation.setStatus(ReservationStatus.REFUND_PENDING);

        if (refundAmount <= 0) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            return 0;
        }

        String finalReason = (reason == null || reason.isBlank()) ? "예약 취소" : reason;
        portoneCancelService.cancelPayment(payment.getMerchantUid(), finalReason, refundAmount);

        payment.setStatus("CANCELLED");
        payment.setCancelledAt(LocalDateTime.now());

        reservation.setStatus(ReservationStatus.CANCELLED);

        return refundAmount;
    }


    private int computeRefundPercent(
            LocalDateTime now,
            LocalDateTime reservationDateTime,
            java.time.LocalDate reservationDate,
            int partySize,
            ReservationStatus currentStatus
    ) {
        if (currentStatus == ReservationStatus.NO_SHOW) return 0;
        if (!now.isBefore(reservationDateTime)) return 0;

        // [TEST] 전일 100% 비활성화
        // if (now.toLocalDate().isBefore(reservationDate)) return 100;

        // [TEST] 예약시간 23시간 전까지(포함): 50%
        //if (!now.isAfter(reservationDateTime.minusHours(18))) return 50;

        return partySize >= 8 ? 10 : 20;
    }



}
