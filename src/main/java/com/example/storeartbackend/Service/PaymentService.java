package com.example.storeartbackend.Service;

import com.example.storeartbackend.DTO.PaymentDTO;
import com.example.storeartbackend.Entity.PaymentEntity;
import com.example.storeartbackend.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    // 결제 정보 저장
    public PaymentDTO savePayment(PaymentDTO paymentDTO) {
        System.out.println("savePayment로 온 paymentDTO : " + paymentDTO);
        // DTO -> Entity 변환
        PaymentEntity paymentEntity = modelMapper.map(paymentDTO, PaymentEntity.class);

        // 데이터베이스에 저장
        PaymentEntity savedEntity = paymentRepository.save(paymentEntity);

        // 저장된 데이터 Entity -> DTO 변환
        return modelMapper.map(savedEntity, PaymentDTO.class);
    }
}