package com.yuvoride.backend.service.impl;

import com.yuvoride.backend.dto.vehicle.VehicleTypeResponse;
import com.yuvoride.backend.entity.VehicleType;
import com.yuvoride.backend.exception.ResourceNotFoundException;
import com.yuvoride.backend.mapper.VehicleMapper;
import com.yuvoride.backend.repository.VehicleTypeRepository;
import com.yuvoride.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VehicleTypeResponse> listActive() {
        return vehicleTypeRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleType getActive(Long id) {
        return vehicleTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle type not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleType resolveActive(Long id, String name) {
        if (id != null) {
            return getActive(id);
        }
        if (name == null || name.isBlank()) {
            throw new ResourceNotFoundException("Vehicle type not found");
        }
        return vehicleTypeRepository.findByNameIgnoreCase(name.trim())
                .filter(VehicleType::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle type not found"));
    }
}
