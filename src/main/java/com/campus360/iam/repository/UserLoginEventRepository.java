package com.campus360.iam.repository;

import com.campus360.iam.domain.UserLoginEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserLoginEventRepository extends JpaRepository<UserLoginEvent, Long> {
    Page<UserLoginEvent> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<UserLoginEvent> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
