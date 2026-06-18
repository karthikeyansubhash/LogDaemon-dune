#!/bin/bash

# HP Enterprise GitHub MCP Server 테스트 스크립트

echo "🚀 HP Enterprise GitHub MCP Server 연결 테스트 중..."

# 환경 변수 설정
export GITHUB_PERSONAL_ACCESS_TOKEN="<GITHUB_PERSONAL_ACCESS_TOKEN>"
export GITHUB_HOST="https://github.azc.ext.hp.com"

echo "✅ 환경 변수 설정 완료"
echo "   - GitHub Host: $GITHUB_HOST"
echo "   - Token: ${GITHUB_PERSONAL_ACCESS_TOKEN:0:8}..."

# MCP 서버 버전 확인
echo ""
echo "🔍 MCP 서버 버전 확인..."
docker run -i --rm \
  -e GITHUB_PERSONAL_ACCESS_TOKEN="$GITHUB_PERSONAL_ACCESS_TOKEN" \
  -e GITHUB_HOST="$GITHUB_HOST" \
  ghcr.io/github/github-mcp-server --version

echo ""
echo "📋 사용 가능한 도구 세트:"
echo "   - repos: 리포지토리 관리"
echo "   - issues: 이슈 관리"
echo "   - pull_requests: PR 관리"
echo "   - actions: GitHub Actions"
echo "   - code_security: 코드 보안"
echo "   - users: 사용자 관리"
echo "   - orgs: 조직 관리"

echo ""
echo "🎯 다음 단계:"
echo "1. VS Code 재시작"
echo "2. Copilot Chat 열기"
echo "3. Agent 모드 토글"
echo "4. MCP 서버 자동 연결 확인"
echo ""
echo "💡 테스트 명령어:"
echo "   - 'HP GitHub에서 내 리포지토리 목록을 보여줘'"
echo "   - 'LogDaemon-dune 프로젝트 정보를 가져와'"
echo "   - '최근 이슈들을 확인해'"
echo ""
echo "🎉 HP Enterprise GitHub MCP Server 설정 완료!"
