--INSERT INTO USERS(username, email, password, createdat)
--VALUES('test', 'test@test.com', '$2a$12$JRP8vS.dBHiGYBAkT69K9.UEt/F1P09jYVhUAAfwTJMhiKK8HjLxu', '20202020'); --123


-- private Long id;
--
--     @Column(unique = true, nullable = false)
--     private String tag;
--
--     @ManyToMany(mappedBy = "hashtags")
--     private Set<Photo> photos = new HashSet<>();
--
--     private Long usageCount = 0L;

INSERT INTO HASHTAGS(tag)
VALUES ('tag');