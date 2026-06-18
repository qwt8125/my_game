package com.paly.legend.auth;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Account> accountMapper = (rs, rowNum) -> {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setUsername(rs.getString("username"));
        account.setPasswordHash(rs.getString("password_hash"));
        account.setStatus(rs.getInt("status"));
        return account;
    };

    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean usernameExists(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM accounts WHERE username = ?",
                Integer.class,
                username);
        return count != null && count > 0;
    }

    public Long createAccount(final String username, final String passwordHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO accounts(username, password_hash) VALUES(?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Account findByUsername(String username) {
        List<Account> accounts = jdbcTemplate.query(
                "SELECT id, username, password_hash, status FROM accounts WHERE username = ?",
                accountMapper,
                username);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public Account findById(long accountId) {
        List<Account> accounts = jdbcTemplate.query(
                "SELECT id, username, password_hash, status FROM accounts WHERE id = ?",
                accountMapper,
                accountId);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    public void updatePasswordHash(long accountId, String passwordHash) {
        jdbcTemplate.update(
                "UPDATE accounts SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                passwordHash,
                accountId);
    }

    public void updateStatus(long accountId, int status) {
        jdbcTemplate.update(
                "UPDATE accounts SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                status,
                accountId);
    }

    public void saveToken(long accountId, String token, LocalDateTime expiresAt) {
        jdbcTemplate.update(
                "INSERT INTO auth_tokens(account_id, token, expires_at) VALUES(?, ?, ?)",
                accountId,
                token,
                FORMATTER.format(expiresAt));
    }

    public TokenRecord findToken(String token) {
        List<TokenRecord> tokens = jdbcTemplate.query(
                "SELECT account_id, token, expires_at, revoked FROM auth_tokens WHERE token = ?",
                (rs, rowNum) -> new TokenRecord(
                        rs.getLong("account_id"),
                        rs.getString("token"),
                        LocalDateTime.parse(rs.getString("expires_at"), FORMATTER),
                        rs.getInt("revoked") == 1),
                token);
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    public void writeLoginLog(Long accountId, String ip, String userAgent, boolean success, String reason) {
        if (accountId == null) {
            return;
        }
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO login_logs(account_id, ip, user_agent, success, reason) VALUES(?, ?, ?, ?, ?)");
            ps.setLong(1, accountId);
            if (ip == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, ip);
            }
            if (userAgent == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, userAgent);
            }
            ps.setInt(4, success ? 1 : 0);
            if (reason == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, reason);
            }
            return ps;
        });
    }
}
