package app.wio.service;

import app.wio.dto.request.ShareRequestDto;
import app.wio.dto.response.ShareResponseDto;
import app.wio.entity.*;
import app.wio.exception.ResourceNotFoundException;
import app.wio.repository.BookingRepository;
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

    @Autowired
    public ShareService(ShareRepository shareRepository,
                        UserRepository userRepository,
                        BookingRepository bookingRepository) {
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
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
        share.setBookingIdsCsv(bookingIds.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")));
        share.setMessage(dto.getMessage());
        share.setCreatedAt(LocalDateTime.now());
        share.setReadAt(null);
        share.setMaxBookingDate(maxDate);

        Share saved = shareRepository.save(share);
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

    private ShareResponseDto toDto(Share s) {
        // parse booking IDs from CSV
        List<Long> bookingIds = new ArrayList<>();
        if (s.getBookingIdsCsv() != null && !s.getBookingIdsCsv().trim().isEmpty()) {
            bookingIds = Arrays.stream(s.getBookingIdsCsv().split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

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
