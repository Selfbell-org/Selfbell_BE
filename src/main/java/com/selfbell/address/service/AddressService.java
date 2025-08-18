package com.selfbell.address.service;

import com.selfbell.address.domain.Address;
import com.selfbell.address.dto.AddressCreateRequest;
import com.selfbell.address.dto.AddressListResponse;
import com.selfbell.address.dto.AddressResponse;
import com.selfbell.address.dto.AddressUpdateRequest;
import com.selfbell.address.exception.AddressNotFoundException;
import com.selfbell.address.repository.AddressRepository;
import com.selfbell.user.domain.User;
import com.selfbell.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.selfbell.global.error.ErrorCode.ADDRESS_NOT_FOUND;

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

    public void update(Long userId, Long addressId, AddressUpdateRequest request) {
        User user = userService.findByIdOrThrow(userId);
        Address address = findByIdOrThrow(addressId);

        if (!address.getUser().equals(user)) {
            throw new AddressNotFoundException(ADDRESS_NOT_FOUND, "해당 주소는 사용자의 주소가 아닙니다.");
        }

        Address updateAddress = request.applyTo(address);

        addressRepository.save(updateAddress);
    }

    private Address findByIdOrThrow(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(ADDRESS_NOT_FOUND, "해당 주소를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public AddressListResponse retrieveAll(Long userId) {
        List<Address> addressList = addressRepository.findAllByUserId(userId);
        List<AddressResponse> responseList = addressList.stream()
                .map(AddressResponse::from)
                .toList();
        return AddressListResponse.of(responseList);
    }
}
