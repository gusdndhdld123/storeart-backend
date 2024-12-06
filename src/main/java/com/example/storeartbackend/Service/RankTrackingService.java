package com.example.storeartbackend.Service;

import com.example.storeartbackend.Repository.RankTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankTrackingService {
    private final RankTrackingRepository rankTrackingRepository;
}
