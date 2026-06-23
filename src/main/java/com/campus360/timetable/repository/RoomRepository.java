package com.campus360.timetable.repository;

import com.campus360.timetable.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Room> findByTenantIdAndRoomCode(Long tenantId, String roomCode);

    List<Room> findByTenantIdAndIsActiveTrue(Long tenantId);
}
