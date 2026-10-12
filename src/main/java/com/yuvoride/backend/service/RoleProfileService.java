package com.yuvoride.backend.service;

import com.yuvoride.backend.entity.Driver;
import com.yuvoride.backend.entity.User;

public interface RoleProfileService {
    void ensure(User user);

    void ensureDriverKyc(Driver driver);
}
