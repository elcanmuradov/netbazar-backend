package com.swaply.userservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRoleConstraintMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void updateUserRoleConstraint() {
        String tableName = resolveLegacyUserTable();
        if (tableName == null) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT IF EXISTS users_user_role_check");
        jdbcTemplate.execute("UPDATE " + tableName + " SET user_role = 'SELLER' WHERE user_role = 'MARKET'");
        jdbcTemplate.execute("UPDATE " + tableName + " SET user_role = 'CUSTOMER' WHERE user_role = 'USER'");
        jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD CONSTRAINT users_user_role_check CHECK (user_role IN ('CUSTOMER', 'ADMIN', 'SELLER'))");
    }

    private String resolveLegacyUserTable() {
        if (hasColumn("users", "user_role")) {
            return "users";
        }

        if (hasColumn("customers", "user_role")) {
            return "customers";
        }

        return null;
    }

    private boolean hasColumn(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }
}