-- 뉴스 임베딩 테이블 생성
CREATE TABLE news_embeddings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_url VARCHAR(500) NOT NULL UNIQUE,
    title VARCHAR(1000) NOT NULL,
    description TEXT NOT NULL,
    publisher VARCHAR(100) NOT NULL,
    embedding_vector LONGTEXT NOT NULL,
    vector_dimension INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_news_url (news_url),
    INDEX idx_publisher (publisher),
    INDEX idx_created_at (created_at)
);
