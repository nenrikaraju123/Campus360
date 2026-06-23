package com.campus360.timetable.service;

import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.timetable.domain.Room;
import com.campus360.timetable.repository.RoomRepository;
import com.campus360.timetable.web.dto.CreateRoomRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepo;
    private final AuditService auditService;

    public RoomService(RoomRepository roomRepo, AuditService auditService) {
        this.roomRepo = roomRepo;
        this.auditService = auditService;
    }

    public Room create(CreateRoomRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        if (roomRepo.findByTenantIdAndRoomCode(tenantId, req.roomCode()).isPresent()) {
            throw ApiException.conflict("Room code already exists: " + req.roomCode());
        }

        Room room = new Room();
        room.setTenantId(tenantId);
        room.setRoomCode(req.roomCode());
        room.setName(req.name());
        room.setBuilding(req.building());
        room.setFloor(req.floor());
        room.setCapacity(req.capacity() != null ? req.capacity() : 0);
        if (req.roomType() != null) room.setRoomType(req.roomType().toUpperCase());
        room = roomRepo.save(room);

        auditService.log("ROOM_CREATED", "Room", room.getId(), "Created room: " + req.roomCode());
        return room;
    }

    @Transactional(readOnly = true)
    public List<Room> listActive() {
        Long tenantId = TenantContext.requireTenantId();
        return roomRepo.findByTenantIdAndIsActiveTrue(tenantId);
    }
}
