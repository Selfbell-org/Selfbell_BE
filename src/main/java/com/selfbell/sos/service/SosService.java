package com.selfbell.sos.service;

import com.selfbell.safewalk.domain.GeoPoint;
import com.selfbell.sos.domain.SosMessage;
import com.selfbell.sos.domain.SosRecipient;
import com.selfbell.sos.dto.SosSendRequest;
import com.selfbell.sos.dto.SosSendResponse;
import com.selfbell.sos.exception.SosMessageEmptyException;
import com.selfbell.sos.repository.SosMessageRepository;
import com.selfbell.sos.repository.SosRecipientRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.exception.UserNotFoundException;
import com.selfbell.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.selfbell.sos.domain.SosMessage.createSosMessage;
import static com.selfbell.sos.domain.SosRecipient.createSosRecipient;

@Service
@RequiredArgsConstructor
@Transactional
public class SosService {

    private final SosMessageRepository sosMessageRepository;
    private final SosRecipientRepository sosRecipientRepository;

    private final UserRepository userRepository;

    public SosSendResponse sendSos(Long userId, SosSendRequest request) {

        validateHasMessage(request.templateId(), request.message());

        GeoPoint point = GeoPoint.of(request.lat(), request.lon());
        SosMessage sosMessage = createSosMessage(userId, request.templateId(), request.message(), point);

        SosMessage savedSos = sosMessageRepository.save(sosMessage);

        // TODO: 현재 프론트에서 templateId를 받지 않고 있음. 추후 템플릿 기능 구현 시 추가 필요

        for (Long receiverId : request.receiverUserIds()) {
            User receiver = userRepository.findById(receiverId)
                    .orElseThrow(() -> new UserNotFoundException(receiverId));

            SosRecipient sosRecipient = createSosRecipient(savedSos, receiver);
            sosRecipientRepository.save(sosRecipient);
        }

        return SosSendResponse.from(savedSos, sentCount);
    }

    private void validateHasMessage(Long templateId, String message) {
        if (templateId == null || message.isBlank()){
            throw new SosMessageEmptyException();
        }
    }
}
