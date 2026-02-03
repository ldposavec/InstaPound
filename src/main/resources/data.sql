-- INSERT INTO USERS(username, email, password, createdat)
-- VALUES('test', 'test@test.com', '$2a$12$JRP8vS.dBHiGYBAkT69K9.UEt/F1P09jYVhUAAfwTJMhiKK8HjLxu', '20202020'); --123


-- private Long id;
--
--     @Column(unique = true, nullable = false)
--     private String tag;
--
--     @ManyToMany(mappedBy = "hashtags")
--     private Set<Photo> photos = new HashSet<>();
--
--     private Long usageCount = 0L;

INSERT INTO USER_PACKAGES (package_type, name, description, max_upload_size_in_bytes, daily_upload_limit,
                           max_total_photos, price)
VALUES ('FREE', 'Free Package', 'Basic free package with limited features', 5242880, 5, 50, 0.00);
INSERT INTO USER_PACKAGES (package_type, name, description, max_upload_size_in_bytes, daily_upload_limit,
                           max_total_photos, price)
VALUES ('PRO', 'Pro Package', 'Professional package with extended features', 20971520, 20, 500, 9.99);
INSERT INTO USER_PACKAGES (package_type, name, description, max_upload_size_in_bytes, daily_upload_limit,
                           max_total_photos, price)
VALUES ('GOLD', 'Gold Package', 'Premium package with unlimited features', 52428800, 100, 5000, 29.99);

INSERT INTO HASHTAGS(tag, usage_count) VALUES ('nature', 0);
INSERT INTO HASHTAGS(tag, usage_count) VALUES ('photography', 0);
INSERT INTO HASHTAGS(tag, usage_count) VALUES ('travel', 0);
INSERT INTO HASHTAGS(tag, usage_count) VALUES ('portrait', 0);
INSERT INTO HASHTAGS(tag, usage_count) VALUES ('landscape', 0);

--password: admin123
INSERT INTO USERS (username, email, password, role, auth_provider, package_type, created_at, current_photo_count,
                   today_upload_count, total_storage_used)
VALUES ('admin', 'admin@instapound.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.6Uc7ThQFljIPLO', 'ADMIN',
        'LOCAL', 'GOLD', CURRENT_TIMESTAMP, 0, 0, 0);

-- password: user123
INSERT INTO USERS (username, email, password, role, auth_provider, package_type, created_at, current_photo_count, today_upload_count, total_storage_used)
VALUES ('testuser', 'user@instapound.com', '$2a$12$eX7vQQhL6Q2FKl8aK.LuQe/9mKXiRZr.n5w9K8yh5BFZQVGM.1hXK', 'REGISTERED', 'LOCAL', 'FREE', CURRENT_TIMESTAMP, 0, 0, 0);

