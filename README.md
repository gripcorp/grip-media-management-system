# grip-media-management-system

라이브 방송을 간단히 시작·종료하고, 방송 상태와 VOD 저장을 관리하는 미디어 관리 서비스입니다. 
스트리밍 공급자는 AWS IVS 또는 BytePlus 중에서 선택적으로 구성할 수 있도록 설계되었습니다.

## 목적

- 방송 송출 시작/종료 제어
- 방송 상태 조회 및 관리
- 방송 녹화본(VOD) 저장 및 조회
- 스트리밍 공급자 추상화: IVS 또는 BytePlus 지원(구성에 따라 선택)

## 빠른 시작

포트: 8090

빌드/실행

```bash
# 빌드
./gradlew build

# 실행 (기본 local 프로파일)
./gradlew bootRun
```

## 최소 설정

프로파일: local, dev, real 을 지원합니다. 각 프로파일별 설정 파일은 `src/main/resources-{profile}` 하위에 있습니다.

필수 개념만 정리하면 다음과 같습니다.

- 데이터베이스/캐시: `config/database.properties`에서 JDBC, Redis 를 설정합니다.

## API 안내(요약)

세부 방송 제어/상태/VOD 관련 API는 공급자 및 배포 환경 설정에 따라 달라질 수 있으므로 Swagger 문서를 참고하세요.

## 기술 스택(간단 요약)

- Kotlin, Spring Boot (Java 21)
- MySQL, Redis
- 스트리밍: AWS IVS 또는 BytePlus

필요 시 더 자세한 설정/CI/CD/모니터링 문서는 별도 문서로 분리하여 관리하세요.

### APM

- **Datadog APM**: 자동으로 활성화 (`dd-java-agent.jar`)
- **JMX Exporter**: Prometheus JMX 메트릭 수집
- **OpenTelemetry**: 분산 추적 (선택 사항)

## 개발 가이드

### 새 환경 추가

1. `src/main/resources-{profile}` 디렉토리 생성
2. `application.properties` 및 `config/*.properties` 파일 작성
3. `build.gradle.kts`에서 프로파일 확인

### 코드 스타일

- Kotlin 공식 스타일 가이드 준수
- 클래스명: PascalCase
- 함수/변수명: camelCase
- 상수: UPPER_SNAKE_CASE

### Properties 파일 규칙

- 환경별 설정은 `resources-{profile}` 디렉토리에 위치
- 공통 설정은 `config/common.properties`
- 민감 정보는 환경 변수 사용 (`${ENV_VAR}`)

## 배포

### Jenkins 자동 배포

## 트러블슈팅

### 빌드 실패

```bash
# Gradle 캐시 정리
./gradlew clean --no-daemon

# 의존성 다시 다운로드
./gradlew build --refresh-dependencies
```

## 라이선스

Copyright © 2025 Grip. All rights reserved.
