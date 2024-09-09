package com.usermanagement.usermanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_session")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "session_id", unique = true)
    private UUID sessionId = UUID.randomUUID();
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "enable")
    private boolean isActive = true;
    @Column(name = "created_date")
    private Date createdDate;
    @Column(name = "updated_date")
    private Date updateDate;

}
