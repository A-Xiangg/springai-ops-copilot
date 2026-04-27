package com.example.opscopilot.entity;

import com.example.opscopilot.support.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统用户实体。
 * 当前主要用于标识聊天会话和知识库等业务数据的归属用户。
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 128)
    private String password;

    @Column(length = 64)
    private String nickname;

    @Column(nullable = false)
    private Short status = 1;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * 首次创建用户时初始化主键、时间和默认状态。
     */
    @PrePersist
    void prePersist() {
        if (id == null) {
            id = IdGenerator.nextId();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = 1;
        }
    }

    /**
     * 用户信息更新时刷新最后修改时间。
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
