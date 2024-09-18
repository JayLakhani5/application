package com.usermanagement.usermanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "user_role_mapping")
public class UserRoleMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role roleId;
    @Column(name = "enable")
    private Boolean enable;
    @Column(name = "created_date")
    private Date createDate;
    @Column(name = "updated_date")
    private Date updateDate;

}
