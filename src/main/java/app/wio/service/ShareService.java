package app.wio.service;

import app.wio.dto.request.ShareRequestDto;
import app.wio.dto.response.ShareResponseDto;
import app.wio.entity.*;
import app.wio.exception.ResourceNotFoundException;
import app.wio.repository.BookingRepository;
import app.wio.repository.ShareBookingRepository;
import app.wio.repository.ShareRepository;
import app.wio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShareService {

    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ShareBookingRepository shareBookingRepository;

    @Autowired
    public ShareService(
            ShareRepository shareRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            ShareBookingRepository shareBookingRepository
    ) {
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.shareBookingRepository = shareBookingRepository;
    }

    @Transactional
    public ShareResponseDto createShare(Long senderId, ShareRequestDto dto) {
        userRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found."));

        List<Long> bookingIds = dto.getBookingIds();
        LocalDate maxDate = LocalDate.now();
        if (!bookingIds.isEmpty()) {
            List<Booking> sharedBookings = bookingRepository.findAllById(bookingIds);
            Optional<LocalDate> possibleMax = sharedBookings.stream()
                    .map(Booking::getDate)
                    .max(LocalDate::compareTo);
            if (possibleMax.isPresent()) {
                maxDate = possibleMax.get();
            }
        }


        Share share = new Share();
        share.setSenderId(senderId);
        share.setRecipientId(dto.getRecipientId());
        share.setMessage(dto.getMessage());
        share.setCreatedAt(LocalDateTime.now());
        share.setReadAt(null);
        share.setMaxBookingDate(maxDate);
        // Bemærk, shareBookings-listen er tom lige nu
        Share saved = shareRepository.save(share);

        // Opret ShareBooking-rækker for hver booking
        for (Long bookingId : bookingIds) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found (ID=" + bookingId + ")"));

            ShareBooking sb = new ShareBooking(saved, booking);
            shareBookingRepository.save(sb);
            // Tilføj til share-objektets liste i memory, hvis du vil have en fuldverdig referencesynkronisering
            saved.getShareBookings().add(sb);
        }

        // Returner en DTO
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ShareResponseDto> getSharesForRecipient(Long recipientId) {
        var shares = shareRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        return shares.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ShareResponseDto markShareAsRead(Long shareId, Long recipientId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));

        if (!share.getRecipientId().equals(recipientId)) {
            throw new ResourceNotFoundException("Not your share to read");
        }
        if (share.getReadAt() == null) {
            share.setReadAt(LocalDateTime.now());
            shareRepository.save(share);
        }
        return toDto(share);
    }


    @Transactional
    public ShareResponseDto markShareAsUnread(Long shareId, Long recipientId) {
        Share share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found"));
        if (!share.getRecipientId().equals(recipientId)) {
            throw new ResourceNotFoundException("Not your share to mark unread");
        }
        if (share.getReadAt() != null) {
            share.setReadAt(null);
            shareRepository.save(share);
        }
        return toDto(share);
    }

    // -- Hjælpe-metode til at bygge en ShareResponseDto --
    private ShareResponseDto toDto(Share s) {
        // Hent booking-IDs via shareBookings-listen
        List<Long> bookingIds = s.getShareBookings().stream()
                .map(ShareBooking::getBooking)
                .map(Booking::getId)
                .collect(Collectors.toList());

        return new ShareResponseDto(
                s.getId(),
                s.getSenderId(),
                s.getRecipientId(),
                bookingIds,
                s.getMessage(),
                s.getCreatedAt(),
                s.getReadAt(),
                s.getMaxBookingDate()
        );
    }
}
