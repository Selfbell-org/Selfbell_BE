package com.selfbell.address.service;

import com.selfbell.address.domain.Address;
import com.selfbell.address.dto.AddressCreateRequest;
import com.selfbell.address.repository.AddressRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    private final UserService userService;

    @Transactional
    public Long create(Long userId, AddressCreateRequest request) {
        User user = userService.findByIdOrThrow(userId);

        Address address = Address.create(
                user,
                request.name(),
                request.address(),
                BigDecimal.valueOf(request.lat()),
                BigDecimal.valueOf(request.lon())
        );
        Address savedAddress = addressRepository.save(address);
        return savedAddress.getId();
    }
}
