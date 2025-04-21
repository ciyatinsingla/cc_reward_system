package com.lms.ccrp.util;

import com.lms.ccrp.enums.Role;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.service.RewardHistoryService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthUtil {
    @Autowired
    private RewardHistoryService rewardHistoryService;
    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Authenticates the admin user based on the provided authorization header.
     * <p>
     * This method validates the JWT token by retrieving the user map from the token
     * and checks whether the user has the ADMIN role. If valid, returns the user ID.
     * </p>
     *
     * @param authHeader The Authorization header containing the Bearer token.
     * @return The user ID of the authenticated admin.
     * @throws RuntimeException if the token is invalid, expired, or the user is not an admin.
     */
    public long authenticateAdminAndFetchId(String authHeader) throws Exception {
        Map<String, Long> userMap = rewardHistoryService.getRequesterId(authHeader);
        if (MapUtils.isEmpty(userMap)) {
            throw new RuntimeException("Invalid or expired token");
        }
        if (!userMap.containsKey(Role.ADMIN.name())) {
            throw new RuntimeException("Action not allowed");
        }
        return userMap.get(Role.ADMIN.name());
    }

    public long authenticateUserAndFetchId(String authHeader) throws Exception {
        Map<String, Long> userMap = rewardHistoryService.getRequesterId(authHeader);
        if (MapUtils.isEmpty(userMap)) {
            throw new RuntimeException("Invalid or expired token");
        }
        if (!userMap.containsKey(Role.USER.name())) {
            throw new RuntimeException("Action not allowed");
        }
        return userMap.get(Role.USER.name());
    }

}
