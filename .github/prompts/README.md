# GitHub MCP 프롬프트 모음집

이 폴더에는 GitHub MCP 서버를 활용한 다양한 분석 및 작업 자동화 프롬프트가 포함되어 있습니다.

## 📂 사용 가능한 프롬프트

### 1. 브랜치 분석 프롬프트
- **`branch-analysis-comprehensive.md`** - 포괄적인 브랜치 분석
  - 커밋 히스토리, 파일 변경사항, 코드 분석, 영향도 평가 포함
  - 상세한 통계와 권장사항 제공
  - 코드 리뷰 준비용으로 적합

- **`branch-analysis-simple.md`** - 간단한 브랜치 분석
  - 핵심 변경사항만 빠르게 파악
  - 일일 개발 진행상황 확인용
  - 빠른 의사결정이 필요한 경우

- **`branch-analysis-mcp-only.md`** - GitHub MCP 전용 브랜치 분석 ⭐
  - 터미널 명령어 없이 GitHub MCP만 사용
  - 404 오류 해결 방법 포함
  - 단계별 상세 가이드 제공
  - **오직 사용자가 지정한 두 브랜치만 분석**

### 2. Pull Request 관련 프롬프트
- **`pull-request-generator.md`** - PR 생성 자동화 프롬프트
  - 브랜치 변경사항을 기반으로 PR 설명 자동 생성
  - 체크리스트와 리뷰 포인트 포함

## 🚀 사용 방법

1. **VS Code에서 GitHub Copilot Chat 열기**
2. **해당 프롬프트 파일 열기**
3. **프롬프트 내용 복사**
4. **Chat에 붙여넣기**
5. **필요한 경우 브랜치명/리포지토리 정보 수정**

## ⚙️ 사전 요구사항

- GitHub MCP 서버가 설정되어 있어야 함
- `.vscode/mcp.json`에 HP Enterprise GitHub 설정 필요
- 적절한 GitHub 토큰 권한 필요

## 🔧 MCP 설정 확인

현재 MCP 설정 상태를 확인하려면:
```bash
# MCP 서버 상태 확인
./test-hp-github-mcp.sh

# MCP 설정 파일 확인
cat .vscode/mcp.json
```

## 📝 커스터마이징 가이드

### 다른 리포지토리 분석
```
현재: <REPOSITORY_PATH>(ex.workpath-dune/LogDaemon-dune)
변경: [your-org/your-repo]
```

### 다른 브랜치 설정
```
현재: topics/junpyo.kim@hp.com/UpdateGradleAndCodeForDune
변경: [your-branch-name]
```

### 기준 브랜치 변경
```
현재: master
변경: main, develop, release/* 등
```

## 🌟 활용 시나리오

### 코드 리뷰 준비
1. `branch-analysis-comprehensive.md` 사용
2. 상세한 변경사항 분석 결과 확인
3. 리뷰어에게 제공할 컨텍스트 준비

### Pull Request Content 생성
1. `pull-request-content-generator.md` 사용
2. 자동 생성된 PR 설명 검토
3. 필요한 경우 추가 정보 보완

### Pull Request 생성
1. `pull-request-auto-generator.md` 사용
2. 자동 생성된 PR 설명 검토
3. 필요한 경우 추가 정보 보완

## 🔍 문제 해결

### GitHub MCP 404 오류
가장 흔한 오류는 브랜치명을 직접 `mcp_github_get_commit`에 사용하는 것입니다.

```
❌ 잘못된 사용법:
mcp_github_get_commit(sha="topics/junpyo.kim@hp.com/UpdateGradleAndCodeForDune")

✅ 올바른 사용법:
1. mcp_github_list_commits(sha="topics/junpyo.kim@hp.com/UpdateGradleAndCodeForDune")
2. 결과에서 실제 커밋 SHA 확인 (예: "6a21dd1...")
3. mcp_github_get_commit(sha="6a21dd1...")
```

**추천:** `branch-analysis-mcp-only.md` 프롬프트를 사용하세요.

### MCP 연결 오류
```bash
# Docker 컨테이너 재시작
docker restart github-mcp-server

# 설정 파일 재확인
cat .vscode/mcp.json
```

### 권한 오류
- GitHub 토큰 만료 여부 확인
- 토큰 권한 범위 확인 (repo, read:org 등)
- HP Enterprise GitHub 접근 권한 확인

---

**마지막 업데이트:** 2025-07-04
**작성자:** Junpyo Kim (junpyo.kim@hp.com)
**버전:** v1.0
