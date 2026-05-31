package vn.icktmeanz.trafficViolation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.icktmeanz.trafficViolation.entity.DetectedViolation;

public interface DetectedViolationRepository extends JpaRepository<DetectedViolation, Long> {
}
