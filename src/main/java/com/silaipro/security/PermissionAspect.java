package com.silaipro.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(hasPermission)")
    public void checkPermission(HasPermission hasPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String requiredPermission = hasPermission.value();
        String userRole = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");

        // Simple role-based mapping for permissions
        boolean hasAccess = false;
        
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("User role information is not available");
        }

        String permissionsJson = userDetails.getUser().getRole().getPermissionsJson();
        if (permissionsJson == null || permissionsJson.isEmpty()) {
            throw new AccessDeniedException("User has no permissions assigned");
        }

        // Check for ALL_PERMISSIONS shortcut (for Admin) or the specific permission string
        hasAccess = permissionsJson.contains("ALL_PERMISSIONS") || permissionsJson.contains(requiredPermission);

        if (!hasAccess) {
            throw new AccessDeniedException("User does not have permission: " + requiredPermission);
        }

    }
}
