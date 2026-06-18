# GitHub MCP Server 설치 및 설정 가이드

## 🎯 설치 완료 항목

### ✅ GitHub MCP Server (HP Enterprise)
- **Docker 이미지**: `ghcr.io/github/github-mcp-server:latest`
- **설정 파일**: `.vscode/mcp.json`
- **GitHub 서버**: `https://github.azc.ext.hp.com`
- **토큰**: 설정 완료 (e450a9ef28d3c98adefc9d0500e0d5eb06e56c65)
- **상태**: 설치 및 설정 완료 ✅

### ✅ Filesystem MCP Server
- **패키지**: `@modelcontextprotocol/server-filesystem`
- **작업 디렉토리**: `/work/git/LogDaemon-dune`
- **상태**: 설정 완료

## 📋 다음 단계

### 1. GitHub Personal Access Token ✅ 완료
- **HP Enterprise GitHub**: `https://github.azc.ext.hp.com`
- **토큰**: 설정 완료
- **상태**: 연결 테스트 성공

### 2. VS Code에서 MCP 활성화
1. VS Code 재시작
2. Copilot Chat 열기
3. Agent 모드 토글 (채팅 입력창 근처 버튼)
4. GitHub Personal Access Token 입력
5. MCP 서버 연결 확인

### 3. 사용 가능한 기능

#### GitHub MCP Server
- 리포지토리 정보 조회
- 이슈 생성/수정/조회
- Pull Request 관리
- GitHub Actions 워크플로우 관리
- 코드 보안 스캔 결과 조회
- 사용자/조직 정보 조회

#### Filesystem MCP Server
- 프로젝트 파일 탐색
- 파일 내용 읽기/쓰기
- 디렉토리 구조 분석

## 🚀 사용 예시

### GitHub 관련 질문
```
- "이 리포지토리의 최근 이슈들을 보여줘"
- "새로운 이슈를 생성해줘"
- "PR 목록을 가져와"
- "GitHub Actions 워크플로우 상태를 확인해"
```

### 파일 시스템 관련 질문
```
- "프로젝트 구조를 분석해줘"
- "Java 파일들을 찾아서 분석해"
- "AndroidManifest.xml 파일을 읽어줘"
- "HP GitHub에서 내 리포지토리 목록을 보여줘"
- "LogDaemon-dune 프로젝트 정보를 가져와"
- "최근 이슈들을 확인해"
- "내 GitHub 프로필 정보를 알려줘"
```

## 🔧 설정 파일 위치
- **MCP 설정**: `.vscode/mcp.json`
- **설치 스크립트**: `install-github-mcp.sh`
- **한글 설정**: `setup-korean.sh`

## ⚠️ 주의사항
- GitHub Personal Access Token을 안전하게 보관하세요
- 토큰에 최소 필요한 권한만 부여하세요
- 토큰이 유출되면 즉시 재발급하세요

## 🎉 완료!
GitHub MCP Server와 Filesystem MCP Server가 성공적으로 설치되었습니다.
VS Code에서 AI 도구를 사용하여 GitHub와 프로젝트 파일을 더 효율적으로 관리할 수 있습니다!
