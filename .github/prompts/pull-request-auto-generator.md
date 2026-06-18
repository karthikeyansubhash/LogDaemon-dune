# Pull Request Auto Generator

이 프롬프트는 브랜치 분석부터 PR 생성까지 완전히 자동화된 워크플로우를 제공합니다. GitHub MCP 도구만을 사용하여 모든 과정을 자동화합니다.

## 사용 방법

이 프롬프트를 VS Code의 GitHub Copilot Chat에 복사하고, 필요한 정보를 업데이트한 후 실행하세요.

---

## 🚀 완전 자동화된 PR 생성 프롬프트

```
GitHub MCP를 사용하여 다음과 같은 완전 자동화된 Pull Request 생성을 수행해 주세요:

### 📋 기본 설정
- **리포지토리**: `<REPOSITORY_PATH>(ex.workpath-dune/LogDaemon-dune)`
- **현재 브랜치**: `topics/junpyo.kim@hp.com/UpdateGradleAndCodeForDune`
- **타겟 브랜치**: `<MASTER_BRANCH_NAME>(ex.mater_dune)`
- **PR 제목**: `[DUNE-79662] Test Creation - Update LogDaemon for Dune `

⚠️ **중요**: 위의 브랜치 정보를 현재 작업 중인 브랜치에 맞게 수정해서 사용하세요.

### 🔧 작업 방식
1. **GitHub MCP 도구만 사용**: 모든 분석과 PR 생성은 GitHub MCP 도구만을 이용하여 수행
2. **브랜치 분석 기반**: `branch-analysis-comprehensive.md` 프롬프트를 활용하여 포괄적인 브랜치 분석 수행
3. **콘텐츠 생성 구조**: `pull-request-content-generator.md` 프롬프트 구조를 따라 PR 콘텐츠 생성

### 🎯 자동화 워크플로우

다음 단계들을 **순차적으로** 자동 실행해 주세요:

#### 1단계: 포괄적인 브랜치 분석 🔍

**GitHub MCP 도구를 사용하여** `branch-analysis-comprehensive.md` 프롬프트 방식으로 다음 내용을 포함한 완전한 브랜치 분석을 수행해 주세요:

**A. 기본 정보 수집**
- 현재 브랜치와 타겟 브랜치의 최신 커밋 정보
- 브랜치 생성 시점과 최근 업데이트 시점

**B. 커밋 히스토리 분석**
- 먼저 `mcp_github_list_pull_requests(head="브랜치명")`로 기존 PR 확인
- `mcp_github_list_commits(sha="브랜치명")`로 현재 브랜치의 모든 커밋 목록 조회
- `mcp_github_get_commit(sha="실제_SHA")`로 상세 정보 조회
- 각 커밋의 메시지, 작성자, 날짜, 변경 파일 수와 라인 수 수집
- 404 오류 발생 시 해당 커밋 건너뛰고 다음 커밋으로 진행
- 커밋 간 시간 간격 분석

**C. 파일 변경사항 세부 분석**
- **우선순위 1**: 기존 PR이 있는 경우 `mcp_github_get_pull_request_files()`로 전체 파일 변경사항 일괄 조회
- **우선순위 2**: 기존 PR이 없는 경우 각 커밋의 `files` 속성에서 변경 파일 정보 수집
- 수정된 파일 목록 (파일별 추가/삭제 라인 수)
- 새로 추가된 파일 목록
- 삭제된 파일 목록 (있는 경우)
- 파일 타입별 변경 통계 (.java, .gradle, .xml, .md 등)
- **성능 최적화**: 중요 파일만 `mcp_github_get_file_contents()`로 내용 확인

**D. 변경사항 카테고리 분류**
- **의존성 변경**: 라이브러리 버전 업데이트, 새로운 의존성 추가
- **새로운 기능**: 새로 추가된 클래스, 메서드, 기능
- **리팩토링**: 코드 구조 개선, 메서드명 변경, 패키지 재구성
- **설정 변경**: 매니페스트, 빌드 설정, 리소스 파일 변경
- **버그 수정**: 오류 수정, 예외 처리 개선
- **문서화**: README, 주석, 문서 파일 변경

**E. 코드 변경사항 상세 분석**
- 각 Java 파일의 주요 변경사항 (클래스별, 메서드별)
- 새로 추가된 메서드나 클래스의 기능
- 수정된 로직의 핵심 내용
- 삭제된 코드의 영향 분석

**F. 빌드 및 설정 파일 분석**
- Gradle 파일 변경사항 (의존성, 버전 업데이트 등)
- Android 매니페스트 변경사항
- 기타 설정 파일 변경사항

**G. 영향도 분석**
- 주요 기능 변경사항과 새로 추가된 기능
- 수정된 기능의 동작 변화
- 잠재적 호환성 문제
- 코드 품질 및 보안 관련 변경사항

**H. 통계 요약**
- 총 커밋 수, 변경 파일 수, 추가/삭제 라인 수
- 주요 변경 영역 (패키지/모듈별)
- 변경사항의 한 줄 요약과 상세 요약

#### 2단계: PR 콘텐츠 생성 📝

위의 브랜치 분석 결과를 바탕으로 **`pull-request-content-generator.md` 프롬프트 구조를 따라** 다음 구조의 포괄적인 PR 설명을 생성해 주세요:

**A. 요약 섹션**
- 한 줄 요약: 핵심 목적과 주요 변경사항
- 배경: 변경사항의 배경과 동기
- 주요 기술적 결정사항
- 예상되는 효과와 영향

**B. 브랜치 분석 요약**
- 브랜치 정보 (source → target)
- 커밋 수와 기간
- 변경 파일 통계
- 주요 변경 카테고리

**C. 상세 변경사항 분석**
- 파일 통계 (타입별 변경 현황)
- 카테고리별 변경사항 (의존성, 새 기능, 리팩토링, 설정, 버그 수정, 문서화)
- 클래스별 코드 변경사항 (각 클래스의 목적, 변경사항, 영향, 의존성)

**D. 테스트 커버리지 분석**
- 단위 테스트 변경사항
- 계측 테스트 변경사항
- 엣지 케이스 및 에러 시나리오
- 전체 테스트 커버리지 평가

**E. 빌드 및 설정 영향 분석**
- Gradle 변경사항 (빌드 스크립트, 의존성, 빌드 프로세스)
- Android 매니페스트 변경사항
- 리소스 파일 변경사항

**F. 영향 분석**
- 기능적 영향 (핵심 기능, 새 기능, 동작 변경)
- 기술적 영향 (성능, 보안, 호환성, 의존성)
- 품질 영향 (코드 품질, 유지보수성, 테스트)

**G. 권장사항**
- 코드 리뷰 시 주의사항
- 테스트가 필요한 영역
- 배포 시 고려사항

#### 3단계: Pull Request 생성 🔄

PR 콘텐츠가 완성되면 다음을 수행해 주세요:

**A. PR 생성**
- 현재 브랜치에서 타겟 브랜치로 PR 생성
- 위에서 생성한 포괄적인 설명과 첨부할 내용을 모두 영어로 번역한 뒤 PR body에 포함
- 적절한 라벨 추가 (bug fix, enhancement, documentation 등)

**B. PR 정보 확인**
- 생성된 PR의 번호와 URL 제공
- PR 상태 확인
- 변경 파일 목록 확인

**C. 후속 작업 안내**
- 리뷰어 할당 안내
- CI/CD 체크 상태 안내
- 추가 테스트 필요 여부 안내

### 🛠️ 사용할 GitHub MCP 도구들

이 자동화 프로세스에서 **오직 GitHub MCP 도구만을 사용하여** 다음 도구들을 **순차적으로** 사용해 주세요:

### ⚠️ 중요: 오류 방지 가이드라인

**1. 커밋 정보 조회 시 필수 순서:**
```
❌ 잘못된 방법:
mcp_github_get_commit(sha="topics/junpyo.kim@hp.com/UpdateGradleAndCodeForDune") // 404 오류!

✅ 올바른 방법:
1. mcp_github_list_commits(sha="topics/junpyo.kim@hp.com/UpdateGradleAndCodeForDune")
2. 결과에서 커밋 SHA 추출 (예: "27bf1f9f95433d495de7a80a32d81dd8a39b68c6")
3. mcp_github_get_commit(sha="27bf1f9f95433d495de7a80a32d81dd8a39b68c6")
```

**2. 기존 PR 우선 확인:**
- 먼저 `mcp_github_list_pull_requests(head="브랜치명")`로 기존 PR 확인
- 기존 PR이 있으면 `mcp_github_get_pull_request_files()`로 파일 변경사항 확인
- 기존 PR이 없을 때만 커밋 기반 분석 수행

**3. 커밋 상세 분석 최적화:**
- 모든 커밋을 상세 분석하지 말고 **최신 20개 커밋만** 분석
- 404 오류 발생 시 해당 커밋 건너뛰고 다음 커밋 진행
- 각 커밋의 `files` 속성에서 변경 파일 정보 수집

**4. 오류 처리 방법:**
```
만약 mcp_github_get_commit 호출 시 404 오류가 발생하면:
1. 해당 커밋을 건너뛰고 계속 진행
2. 오류 메시지를 기록하되 전체 프로세스는 중단하지 않음
3. 다른 커밋에서 필요한 정보 수집
```

**5. 권장 실행 순서:**
```
1. mcp_github_list_pull_requests(head="브랜치명") // 기존 PR 확인
2. mcp_github_list_commits(sha="브랜치명") // 커밋 목록 획득
3. 최신의 최대 20개 커밋에 대해서만 mcp_github_get_commit() 호출
4. mcp_github_get_pull_request_files() (기존 PR 있는 경우)
5. 필요한 경우에만 mcp_github_get_file_contents() 호출
```

1. **브랜치 분석 단계** (`branch-analysis-comprehensive.md` 방식):
   - `mcp_github_list_commits`: 커밋 히스토리 분석
   - `mcp_github_get_commit`: 각 커밋의 상세 정보
   - `mcp_github_get_file_contents`: 변경된 파일 내용 확인
   - `mcp_github_get_pull_request_diff`: 변경사항 diff 확인 (기존 PR이 있는 경우)

2. **PR 콘텐츠 생성 단계** (`pull-request-content-generator.md` 구조):
   - 분석 결과를 바탕으로 포괄적인 PR 설명 생성
   - Executive Summary, 브랜치 분석 요약, 상세 변경사항 분석 포함
   - 테스트 커버리지, 빌드 영향, 영향 분석 등 모든 섹션 포함

3. **PR 생성 단계**:
   - `mcp_github_create_pull_request`: PR 생성
   - `mcp_github_get_pull_request`: 생성된 PR 정보 확인
   - `mcp_github_get_pull_request_files`: PR 파일 변경사항 확인

4. **후속 작업 단계**:
   - `mcp_github_get_pull_request_status`: PR 상태 확인
   - `mcp_github_list_pull_request_reviews`: 리뷰 상태 확인 (필요시)

### 🚨 추가 안전장치

**자동 복구 메커니즘:**
- 커밋 분석 중 연속 3회 404 오류 시 해당 브랜치 분석 중단하고 기존 PR 정보로 대체
- API 호출 간 1-2초 간격 두어 Rate Limit 방지
- 각 MCP 도구 호출 전 필수 파라미터 유효성 검증

**백업 분석 방법:**
```
1차: mcp_github_get_pull_request_files() 사용 (기존 PR 있는 경우)
2차: 최신 커밋들만 개별 분석
3차: mcp_github_list_commits()의 기본 정보만 사용
```

**성능 최적화:**
- 파일 변경사항은 PR 레벨에서 일괄 조회 우선
- 커밋 상세 분석은 꼭 필요한 경우에만 수행

### 🎯 성공 기준

다음 모든 항목이 완료되면 성공입니다:

- [ ] **GitHub MCP 도구만을 사용**하여 포괄적인 브랜치 분석 완료
- [ ] **`branch-analysis-comprehensive.md` 방식**의 상세 분석 완료
- [ ] **`pull-request-content-generator.md` 구조**의 포괄적인 PR 콘텐츠 생성 완료
- [ ] PR 성공적으로 생성됨
- [ ] PR 번호와 URL 제공됨
- [ ] 변경 파일 목록 확인됨
- [ ] 후속 작업 안내 완료

### 📚 참고 프롬프트

이 자동화 워크플로우는 다음 프롬프트들을 기반으로 합니다:
- **`branch-analysis-comprehensive.md`**: 포괄적인 브랜치 분석 방법론
- **`pull-request-content-generator.md`**: PR 콘텐츠 생성 구조 및 템플릿

---

**지금 바로 위의 자동화 워크플로우를 실행해 주세요!**
```

### 💡 추가 옵션

필요에 따라 다음 추가 작업도 **GitHub MCP 도구를 사용하여** 수행할 수 있습니다:

- **리뷰어 할당**: 특정 리뷰어 자동 할당
- **라벨 추가**: 변경사항에 따른 적절한 라벨 추가
- **마일스톤 설정**: 관련 마일스톤 설정
- **Draft PR**: 초안 PR로 먼저 생성 후 확인

## 주의사항

1. **브랜치 정보 정확성**: 현재 작업 중인 브랜치 정보를 정확히 입력하세요.
2. **권한 확인**: PR 생성 권한이 있는지 확인하세요.
3. **네트워크 연결**: GitHub API 호출을 위한 안정적인 네트워크 연결이 필요합니다.
4. **API 제한**: GitHub API 제한에 걸리지 않도록 주의하세요.

## 문제 해결

### 일반적인 오류
- **브랜치 찾을 수 없음**: 브랜치 이름을 다시 확인하세요.
- **권한 없음**: 리포지토리 접근 권한을 확인하세요.
- **API 제한**: 잠시 후 다시 시도하세요.

### 부분 실행
전체 프로세스가 실패하는 경우, 각 단계를 개별적으로 실행할 수 있습니다:
1. 브랜치 분석만 실행
2. PR 콘텐츠 생성만 실행
3. PR 생성만 실행

## 확장 가능성

이 자동화 워크플로우는 다음과 같이 확장할 수 있습니다:
- 다른 브랜치 전략 지원 (feature, develop, release 브랜치)
- 다른 리포지토리 플랫폼 지원
- 자동 테스트 실행
- 자동 배포 트리거
- 슬랙 알림 연동
