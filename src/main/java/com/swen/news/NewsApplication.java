package com.swen.news;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot 애플리케이션의 진입점 클래스입니다.
 *
 * <p>이 클래스는 스프링 부트 자동 설정을 활성화하며, JPA 감사 기능(@EnableJpaAuditing)을 사용하도록 설정합니다.
 */
@SpringBootApplication
@EnableJpaAuditing
public class NewsApplication {

	/**
	 * 애플리케이션을 시작하는 메인 메서드입니다.
	 *
	 * @param args 애플리케이션 시작 시 전달되는 인자들
	 */
	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		SpringApplication.run(NewsApplication.class, args);
	}

}
