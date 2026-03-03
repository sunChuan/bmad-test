DROP TABLE IF EXISTS school;
CREATE TABLE school (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    district_id BIGINT,
    district_name VARCHAR(100),
    school_type VARCHAR(20),
    student_count INT,
    teacher_count INT,
    assessment_score DOUBLE,
    alert_status VARCHAR(20),
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
