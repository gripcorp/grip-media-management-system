import java.text.SimpleDateFormat

pipeline {
    agent {
        label 'backend-pod-jdk21'
    }

    environment {
        appEnv = 'dev'
        branchTag = "${env.BRANCH_NAME.replaceAll('/', '_')}"
        slackChannel = "server-${env.appEnv}-${env.SLACK_CHANNEL}"
    }
    options {
        skipDefaultCheckout(true)
        ansiColor('xterm')
        timestamps()
    }

    stages {
        stage('Initialize') {
            steps {
                script {
                    env.repoName = env.JOB_NAME.tokenize('/')[1]
                    env.imagePath = "${env.DevRepo}/${env.appEnv}/${env.repoName}"
                    env.current_timestamp = (System.currentTimeMillis() / 1000L) as Long
                    env.deployVersion = "${env.branchTag}-${env.current_timestamp}"
                    echo """
                    🔍 JOB_NAME: ${env.JOB_NAME}
                    📦 repoName: ${env.repoName}
                    🖼️  imagePath: ${env.imagePath}
                    """
                }
            }
        }

        stage('Start_Noti') {
            steps {
                script {
                    slackSend(
                        channel: "#${env.slackChannel}",
                        color: '#747474',
                        message: "*[${env.appEnv}] ${env.repoName}:${env.BUILD_NUMBER} 빌드 시작* :hammer_and_wrench: \n| 브랜치: ${env.BRANCH_NAME} \n| 빌드: #${env.BUILD_NUMBER} \n| 이미지: `${env.deployVersion}` \n| <${env.BUILD_URL}/console|로그>"
                    )
                }
            }
        }

        stage('Clone repository') {
            steps {
                checkout scm
            }
        }

        stage('Build app') {
            steps {
                container('jdk21') {
                    script {
                        sh """
                            export GRADLE_USER_HOME=/home/jenkins/.gradle/${env.repoName}
                            SHARED_GRADLE="/home/jenkins/.gradle"
                            
                            # 공유 캐시 디렉토리 생성 (없으면 생성)
                            mkdir -p \${SHARED_GRADLE}/wrapper
                            mkdir -p \${SHARED_GRADLE}/caches/modules-2
                            mkdir -p \${GRADLE_USER_HOME}/caches
                            
                            # Gradle Wrapper 공유 (심볼릭 링크)
                            if [ ! -L "\${GRADLE_USER_HOME}/wrapper" ]; then
                                ln -sfn \${SHARED_GRADLE}/wrapper \${GRADLE_USER_HOME}/wrapper
                            fi
                            
                            # 의존성 캐시 공유 (심볼릭 링크)
                            if [ ! -L "\${GRADLE_USER_HOME}/caches/modules-2" ]; then
                                ln -sfn \${SHARED_GRADLE}/caches/modules-2 \${GRADLE_USER_HOME}/caches/modules-2
                            fi
                            
                            ./gradlew build -Pprofile=${env.appEnv} \\
                                --info \\
                                --build-cache \\
                                --parallel \\
                                --max-workers=4 \\
                                --console=plain \\
                                -Dorg.gradle.vfs.watch=false
                        """
                    }
                }
            }
        }
        
        stage('Build image') {
            steps {
                container('buildkit') {
                    script {
                        def dockerfilePath = 'Dev.Dockerfile'
                        sh """
                            # BuildKit으로 이미지 빌드 및 푸시
                            buildctl build \\
                            --frontend dockerfile.v0 \\
                            --local context=\$(pwd) \\
                            --local dockerfile=\$(pwd) \\
                            --opt filename=${dockerfilePath} \\
                            --opt build-arg:DD_GIT_REPOSITORY_URL=${env.GIT_URL} \\
                            --opt build-arg:DD_GIT_COMMIT_SHA=${env.GIT_COMMIT} \\
                            --export-cache type=registry,ref=${env.imagePath}/cache,mode=max \\
                            --import-cache type=registry,ref=${env.imagePath}/cache \\
                            --output type=image,name=${env.imagePath}:${env.deployVersion},push=true \\
                            --output type=image,name=${env.imagePath}:latest,push=true
                        """
                    }
                }
            }
        }

        stage('Verify Build') {
            steps {
                script {
                    echo """✅ 이미지 빌드 완료: ${env.imagePath}:${env.deployVersion}"""
                }
            }
        }

        stage('Trigger Deploy Job') {
            steps {
                script {
                    echo """
                    🚀 배포 Job 트리거 파라미터:
                    • APP_ENV: ${env.appEnv}
                    • REPO_NAME: ${env.repoName}
                    • DEPLOY_VERSION: ${env.deployVersion}
                    • slackChannel: ${env.slackChannel}
                    """

                    build job: 'dev-deploy-trigger',
                        parameters: [
                            string(name: 'APP_ENV', value: env.appEnv),
                            string(name: 'REPO_NAME', value: env.repoName),
                            string(name: 'DEPLOY_VERSION', value: env.deployVersion),
                            string(name: 'SLACK_CHANNEL', value: env.slackChannel)
                        ],
                        wait: false
                }
            }
        }
    }

    post {
        success {
            script {
                slackSend(
                    channel: "#${env.slackChannel}",
                    color: 'good',
                    message: "*[${env.appEnv}] \"${env.repoName}\" 빌드 성공* :white_check_mark: \n| 브랜치: ${env.BRANCH_NAME} \n| 빌드: #${env.BUILD_NUMBER} \n| 이미지: `${env.deployVersion}` \n| <${env.BUILD_URL}/console|로그>"

                )
            }
        }
        failure {
            script {
                slackSend(
                    channel: "#${env.slackChannel}",
                    color: 'danger',
                    message: "*[${env.appEnv}] \"${env.repoName}\" 빌드 실패* :x: \n| 브랜치: ${env.BRANCH_NAME} \n| 빌드: #${env.BUILD_NUMBER} \n| 이미지: `${env.deployVersion}` \n| <${env.BUILD_URL}/console|로그>"
                )
            }
        }
        aborted {
            script {
                slackSend(
                    channel: "#${env.slackChannel}",
                    color: 'warning',
                    message: "*[${env.appEnv}] \"${env.repoName}\" 빌드 취소* :ballot_box_with_check: \n| 브랜치: ${env.BRANCH_NAME} \n| 빌드: #${env.BUILD_NUMBER} \n| 이미지: `${env.deployVersion}` \n| <${env.BUILD_URL}/console|로그>"
                )
            }
        }
                cleanup {
                    cleanWs(
                        deleteDirs: true,
                        notFailBuild: true,
                        patterns: [
                            [pattern: 'node_modules', type: 'INCLUDE']
                        ]
                    )
                }
    }
}