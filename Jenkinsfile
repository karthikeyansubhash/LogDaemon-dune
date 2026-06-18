pipeline {
    agent {
        docker {
            image 'apk-builder:ubuntu22.04-v2'
            args """
                --privileged
                -v /var/run/docker.sock:/var/run/docker.sock
                -v /work/jenkins/workspace:/var/jenkins_home/workspace
                -v /work/jenkins/workspace:/aosp
                --user 1000:988
                --shm-size=8g
                --memory=16g
                --cpus=8
            """
            reuseNode true
        }
    }

    parameters {
        // 브랜치 선택 파라미터
        string(
            name: 'BRANCH_NAME',
            defaultValue: 'master_dune',
            description: '빌드할 Git 브랜치를 입력하세요 (예: master, topics, feature/xxx)'
        )
        
        // 빌드 타입 선택 파라미터
        choice(
            name: 'BUILD_TYPE',
            choices: ['Both', 'Release Only', 'Release_sim Only'],
            description: '빌드할 APK 타입을 선택하세요'
        )
        
        // 클린 빌드 여부
        booleanParam(
            name: 'CLEAN_BUILD',
            defaultValue: false,
            description: '전체 clean 빌드를 수행할지 선택하세요'
        )
    }

    triggers {
        // GitHub 푸시 이벤트가 발생하면 빌드를 트리거합니다.
        githubPush()
        // SCM 폴링도 함께 설정 (fallback)
        pollSCM('H/5 * * * *')
    }

    environment {
        // Android SDK 경로 설정 (Jenkins 서버에 Android SDK가 설치되어 있어야 함)
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        PATH = "${env.ANDROID_HOME}/platform-tools:${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/tools/bin:${env.PATH}"

        // Gradle 설정 - 빌드 성능 최적화
        GRADLE_OPTS = '-Dorg.gradle.daemon=true -Dorg.gradle.caching=true -Xmx4g -XX:MaxMetaspaceSize=512m'
    }

    options {
        // 빌드 히스토리 관리
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // 타임아웃 설정
        timeout(time: 45, unit: 'MINUTES')
        // 동시 빌드 방지
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    // 빌드가 어떻게 트리거되었는지 확인
                    def buildCauses = currentBuild.getBuildCauses()
                    def isGitHubPush = buildCauses.any { it._class.contains('GitHubPushCause') || it._class.contains('SCMTriggerCause') }
                    def isManualBuild = buildCauses.any { it._class.contains('UserIdCause') }
                    
                    echo "Build causes: ${buildCauses}"
                    echo "Is GitHub Push: ${isGitHubPush}"
                    echo "Is Manual Build: ${isManualBuild}"
                    
                    if (isGitHubPush && !isManualBuild) {
                        // 자동 빌드 (Git push): SCM에서 감지된 브랜치로 체크아웃
                        echo "🔄 Auto build detected - using SCM detected branch"
                        checkout scm
                        
                        // Jenkins 환경 변수에서 브랜치명 추출
                        def gitBranch = env.GIT_BRANCH ?: env.BRANCH_NAME ?: 'unknown'
                        if (gitBranch.startsWith('origin/')) {
                            gitBranch = gitBranch.replaceFirst('origin/', '')
                        }
                        env.ACTUAL_BRANCH = gitBranch
                        echo "🌿 Auto build branch from env: ${env.ACTUAL_BRANCH}"
                        
                    } else {
                        // 수동 빌드: 파라미터로 지정된 브랜치로 체크아웃
                        echo "🔄 Manual build detected - using parameter branch: ${params.BRANCH_NAME}"
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: "*/${params.BRANCH_NAME}"]],
                            doGenerateSubmoduleConfigurations: false,
                            extensions: [
                                [$class: 'CleanCheckout'],
                                [$class: 'CloneOption', depth: 1, noTags: false, reference: '', shallow: true]
                            ],
                            submoduleCfg: [],
                            userRemoteConfigs: scm.userRemoteConfigs
                        ])
                        
                        // 수동 빌드에서는 파라미터 값 사용
                        env.ACTUAL_BRANCH = params.BRANCH_NAME
                        echo "🌿 Manual build branch from param: ${env.ACTUAL_BRANCH}"
                    }
                    
                    // 현재 브랜치와 커밋 정보 확인 (디버깅용)
                    sh 'git branch -a'
                    sh 'git log --oneline -5'
                    
                    // Git 환경 변수들 확인 (디버깅용)
                    echo "🔍 Git Environment Debug:"
                    echo "   GIT_BRANCH: ${env.GIT_BRANCH}"
                    echo "   BRANCH_NAME: ${env.BRANCH_NAME}"
                    echo "   GIT_COMMIT: ${env.GIT_COMMIT}"
                    
                    // detached HEAD 상태 확인 및 브랜치명 보정
                    def headCheck = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "   HEAD points to: ${headCheck}"
                    
                    // HEAD인 경우 환경 변수나 파라미터에서 브랜치명 사용
                    if (headCheck == 'HEAD') {
                        echo "⚠️ Detached HEAD detected, using previously determined branch: ${env.ACTUAL_BRANCH}"
                    } else {
                        echo "✅ Branch checkout successful: ${headCheck}"
                        // HEAD가 아닌 경우에만 업데이트
                        if (headCheck != env.ACTUAL_BRANCH) {
                            echo "🔄 Updating branch name from ${env.ACTUAL_BRANCH} to ${headCheck}"
                            env.ACTUAL_BRANCH = headCheck
                        }
                    }
                    
                    echo "📋 Final determined branch: ${env.ACTUAL_BRANCH}"
                }
            }
        }

        stage('Environment Check') {
            steps {
                echo "Checking build environment..."

                script {
                    def currentTime = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Seoul'))
                    def buildCauses = currentBuild.getBuildCauses()

                    echo "Build started at: ${currentTime}"
                    echo "Build causes: ${buildCauses}"
                    echo "Build number: ${env.BUILD_NUMBER}"
                    echo "Git Branch (env.GIT_BRANCH): ${env.GIT_BRANCH}"
                    echo "Git Branch (env.BRANCH_NAME): ${env.BRANCH_NAME}"
                    echo "Git Branch (ACTUAL_BRANCH): ${env.ACTUAL_BRANCH}"
                    echo "Git Commit: ${env.GIT_COMMIT}"
                    echo "Branch Parameter: ${params.BRANCH_NAME}"

                    // 환경 변수 확인
                    sh 'echo "JAVA_HOME: $JAVA_HOME"'
                    sh 'echo "ANDROID_HOME: $ANDROID_HOME"'
                    sh 'java -version'
                    sh 'echo "PATH: $PATH"'

                    // 워크스페이스 확인
                    sh 'pwd && ls -la'
                }
            }
        }

        stage('Android SDK Check') {
            steps {
                script {
                    echo "Checking Android SDK installation..."

                    // 현재 환경 확인
                    sh 'ls -la /opt/ || echo "No /opt directory"'
                    sh 'echo "ANDROID_HOME: $ANDROID_HOME"'

                    // Android SDK 설치 확인 - 셸 명령어로 직접 확인
                    def sdkCheckResult = sh(script: "test -d '${env.ANDROID_HOME}' && echo 'exists' || echo 'not_exists'", returnStdout: true).trim()

                    if (sdkCheckResult != 'exists') {
                        echo "⚠️ Android SDK not found at ${env.ANDROID_HOME}"
                        echo "Available directories in /opt:"
                        sh 'ls -la /opt/ || echo "No /opt directory found"'

                        // SDK 자동 설치 시도 (선택사항)
                        echo "Attempting to verify SDK tools..."
                        sh 'which sdkmanager || echo "sdkmanager not found in PATH"'

                        error "Android SDK not found. Please ensure the correct Docker image is used."
                    }

                    echo "✅ Android SDK found at: ${env.ANDROID_HOME}"

                    // SDK 구성 요소 확인
                    sh 'ls -la $ANDROID_HOME/ || echo "Cannot list SDK contents"'
                    sh 'which adb || echo "adb not found in PATH"'
                    sh 'which sdkmanager || echo "sdkmanager not found in PATH"'

                    // 중요한 SDK 구성 요소들 확인
                    sh '''
                        echo "Checking SDK structure:"
                        test -d $ANDROID_HOME/platform-tools && echo "✅ platform-tools found" || echo "❌ platform-tools missing"
                        test -d $ANDROID_HOME/cmdline-tools && echo "✅ cmdline-tools found" || echo "❌ cmdline-tools missing"
                        test -d $ANDROID_HOME/platforms && echo "✅ platforms found" || echo "❌ platforms missing"
                        test -d $ANDROID_HOME/build-tools && echo "✅ build-tools found" || echo "❌ build-tools missing"
                    '''
                }
            }
        }

        stage('Prepare Build') {
            steps {
                echo "Preparing Android build..."

                // Gradle wrapper 실행 권한 부여
                sh 'chmod +x ./gradlew'

                sh './gradlew --version'
            }
        }

        stage('Clean') {
            steps {
                script {
                    if (params.CLEAN_BUILD) {
                        echo "🧹 Performing full clean build..."
                        sh './gradlew clean'
                    } else {
                        echo "🧹 Cleaning only when necessary..."
                        // 전체 clean 대신 출력 디렉토리만 정리
                        sh 'rm -rf build/modules/app/outputs || echo "No previous APK outputs to clean"'
                        sh 'rm -rf build/modules/app/intermediates || echo "No previous DEX files to clean"'
                    }
                }
            }
        }

        stage('Build Release APK') {
            when {
                anyOf {
                    expression { params.BUILD_TYPE == 'Both' }
                    expression { params.BUILD_TYPE == 'Release Only' }
                }
            }
            steps {
                echo "🔨 Building Android release APK..."
                // 더 많은 불필요한 태스크 스킵으로 빌드 시간 단축
                sh './gradlew assembleRelease -x test -x lint -x lintVitalRelease -x checkReleaseAarMetadata --build-cache --configuration-cache --parallel --max-workers=4'
            }
            post {
                always {
                    // 빌드 결과 확인
                    script {
                        echo "Checking Release APK build results..."
                        sh 'find build/modules/app/outputs -name "*.apk" -type f || echo "No APK files found"'
                        def releaseApkExists = sh(script: "test -d build/modules/app/outputs/apk/release && echo 'exists' || echo 'not_exists'", returnStdout: true).trim()
                        if (releaseApkExists == 'exists') {
                            sh 'ls -la build/modules/app/outputs/apk/release/'
                        }
                    }
                }
            }
        }

        stage('Build Release Sim APK') {
            when {
                anyOf {
                    expression { params.BUILD_TYPE == 'Both' }
                    expression { params.BUILD_TYPE == 'Release_sim Only' }
                }
            }
            steps {
                echo "🔨 Building Android release_sim APK..."
                // 첫 번째 빌드의 캐시를 활용하여 빠른 빌드
                sh './gradlew assembleRelease_sim -x test -x lint -x lintVitalRelease_sim -x checkRelease_simAarMetadata --build-cache --configuration-cache --parallel --max-workers=4'
            }
            post {
                always {
                    // 빌드 결과 확인
                    script {
                        echo "Checking Release Sim APK build results..."
                        def releaseSimApkExists = sh(script: "test -d build/modules/app/outputs/apk/release_sim && echo 'exists' || echo 'not_exists'", returnStdout: true).trim()
                        if (releaseSimApkExists == 'exists') {
                            sh 'ls -la build/modules/app/outputs/apk/release_sim/'
                        }
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                script {
                    // APK 파일 위치 확인 (올바른 경로)
                    echo "Searching for APK files in correct path..."
                    sh 'find build/modules/app/outputs -name "*.apk" -type f || echo "No APK files found"'
                    sh 'ls -la build/modules/app/outputs/ || echo "No outputs directory"'
                    sh 'ls -la build/modules/app/outputs/apk/ || echo "No apk directory"'

                    // 모든 APK 파일을 무조건 아카이브 (올바른 경로)
                    echo "Archiving all APK files..."
                    archiveArtifacts artifacts: 'build/modules/app/outputs/**/*.apk', fingerprint: true, allowEmptyArchive: true
                }

                script {
                    // 빌드 정보 파일 생성
                    def currentTime = new Date().format('yyyy-MM-dd HH:mm:ss', TimeZone.getTimeZone('Asia/Seoul'))
                    def buildCauses = currentBuild.getBuildCauses()
                    def isManualBuild = buildCauses.any { it._class.contains('UserIdCause') }
                    def actualBranch = env.ACTUAL_BRANCH ?: env.GIT_BRANCH
                    
                    def buildInfo = """
Build Information:
==================
Build Time: ${currentTime}
Build Number: ${env.BUILD_NUMBER}
Build Type: ${isManualBuild ? 'Manual' : 'Automatic'}
Git Branch (Actual): ${actualBranch}
Git Commit: ${env.GIT_COMMIT}
Build APK Type: ${params.BUILD_TYPE}
Clean Build: ${params.CLEAN_BUILD}
Java Version: ${sh(script: 'java -version 2>&1 | head -1', returnStdout: true).trim()}
Gradle Version: ${sh(script: './gradlew --version | grep "Gradle"', returnStdout: true).trim()}
APK Location: build/modules/app/outputs/apk/
"""

                    writeFile file: 'build_info.txt', text: buildInfo
                    archiveArtifacts artifacts: 'build_info.txt', fingerprint: true
                }
            }
        }
    }

    post {
        always {
            echo "Build completed!"
        }
        success {
            echo "✅ Build succeeded!"
            script {
                // Release APK 파일들 확인 (올바른 경로)
                def releaseApkExists = sh(script: "test -d build/modules/app/outputs/apk/release && echo 'exists' || echo 'not_exists'", returnStdout: true).trim()
                if (releaseApkExists == 'exists') {
                    echo "Release APK files created successfully:"
                    sh 'find build/modules/app/outputs/apk/release -name "*.apk" -exec ls -lh {} \\;'
                }

                def releaseSimApkExists = sh(script: "test -d build/modules/app/outputs/apk/release_sim && echo 'exists' || echo 'not_exists'", returnStdout: true).trim()
                if (releaseSimApkExists == 'exists') {
                    echo "Release Sim APK files created successfully:"
                    sh 'find build/modules/app/outputs/apk/release_sim -name "*.apk" -exec ls -lh {} \\;'
                }

                // 전체 APK 파일 확인
                echo "All APK files in build outputs:"
                sh 'find build/modules/app/outputs -name "*.apk" -type f -exec ls -lh {} \\; || echo "No APK files found"'
                
                // 🔄 다른 젠킨스 프로젝트 호출 (빌드 성공 시)
                try {
                    echo "🚀 Triggering downstream job..."
                    
                    // 공유 저장소에 아티팩트 복사 (workspace 내부 사용)
                    def sharedPath = "${env.WORKSPACE}/shared_artifacts_${env.BUILD_NUMBER}"
                    try {
                        sh """
                            # 현재 workspace 내에 공유 디렉토리 생성
                            mkdir -p ${sharedPath}
                            
                            # APK 파일들 복사
                            find build/modules/app/outputs -name "*.apk" -type f -exec cp {} ${sharedPath}/ \\; || echo "No APK files to copy"
                            
                            # 빌드 정보 JSON 생성
                            cat > ${sharedPath}/build_info.json << EOF
{
    "source_job": "${env.JOB_NAME}",
    "build_number": "${env.BUILD_NUMBER}",
    "branch": "${env.ACTUAL_BRANCH ?: env.GIT_BRANCH ?: params.BRANCH_NAME ?: 'unknown'}",
    "commit": "${env.GIT_COMMIT}",
    "build_type": "${params.BUILD_TYPE}",
    "clean_build": "${params.CLEAN_BUILD}",
    "timestamp": "\$(date -Iseconds)",
    "artifacts_path": "${sharedPath}"
}
EOF
                            
                            echo "📦 Artifacts copied to: ${sharedPath}"
                            ls -la ${sharedPath}/
                        """
                    } catch (Exception copyError) {
                        echo "⚠️ Failed to copy to shared storage: ${copyError.getMessage()}"
                        sharedPath = "N/A"
                    }
                    
                    // 다운스트림 job 호출
                    try {
                        // 브랜치명 최종 결정 (우선 순위: ACTUAL_BRANCH > GIT_BRANCH > 파라미터)
                        def finalBranch = env.ACTUAL_BRANCH ?: env.GIT_BRANCH ?: params.BRANCH_NAME ?: 'unknown'
                        if (finalBranch.startsWith('origin/')) {
                            finalBranch = finalBranch.replaceFirst('origin/', '')
                        }
                        
                        echo "🌿 Final branch for downstream: ${finalBranch}"
                        
                        def downstreamBuild = build job: 'Dune_All_Application', 
                            parameters: [
                                string(name: 'SOURCE_BUILD_NUMBER', value: env.BUILD_NUMBER),
                                string(name: 'SOURCE_BRANCH', value: finalBranch),
                                string(name: 'APK_BUILD_TYPE', value: params.BUILD_TYPE),
                                string(name: 'SOURCE_JOB_NAME', value: env.JOB_NAME),
                                string(name: 'SOURCE_COMMIT', value: env.GIT_COMMIT ?: 'unknown'),
                                string(name: 'CLEAN_BUILD', value: params.CLEAN_BUILD.toString()),
                                string(name: 'SHARED_ARTIFACTS_PATH', value: sharedPath)
                            ],
                            wait: false,
                            propagate: false
                        
                        if (downstreamBuild) {
                            echo "✅ Downstream job triggered successfully"
                            echo "   Build number: ${downstreamBuild.number}"
                            echo "   Job URL: ${env.JENKINS_URL}job/Dune_All_Application/${downstreamBuild.number}/"
                        } else {
                            echo "⚠️ Downstream job was triggered but returned null"
                        }
                        
                    } catch (Exception buildError) {
                        echo "⚠️ Failed to trigger downstream job: ${buildError.getMessage()}"
                        echo "   This might be because:"
                        echo "   1. 'Dune_All_Application' job doesn't exist"
                        echo "   2. No permission to trigger the job"
                        echo "   3. Job is disabled or not configured"
                        echo "   📝 Please check if 'Dune_All_Application' job exists in Jenkins"
                    }
                    
                } catch (Exception e) {
                    echo "⚠️ Failed to trigger downstream job: ${e.getMessage()}"
                    // 다운스트림 작업 실패가 현재 빌드를 실패시키지 않음
                }
            }
        }
        failure {
            echo "❌ Build failed!"
        }
        unstable {
            echo "⚠️ Build unstable!"
        }
    }
}