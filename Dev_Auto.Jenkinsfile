import java.text.SimpleDateFormat

pipeline {
    agent none

    environment {
        appEnv = 'dev'
        branchTag = "${env.BRANCH_NAME.replaceAll('/', '_')}"
    }
    options {
        skipDefaultCheckout(true)
        ansiColor('xterm')
    }

    stages {
        stage('Build Pipeline') {
            when {
                beforeAgent true
                branch 'phase/dev'
            }
            agent {
                label 'backend-pod-jdk21'
            }
            stages {
                stage('Initialize') {
                    steps {
                        script {
                            env.repoName = env.JOB_NAME.tokenize('/')[1]
                            env.imagePath = "${env.DevRepo}/${env.appEnv}/${env.repoName}"
                            env.current_timestamp = (System.currentTimeMillis() / 1000L) as Long
                            env.deployVersion = "${env.branchTag}-${env.current_timestamp}"
                            env.slackChannel = "server-${env.appEnv}-${env.SLACK_CHANNEL}"
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
                                    mkdir -p \${GRADLE_USER_HOME}
                                    
                                    ./gradlew clean build -Pprofile=${env.appEnv} \\
                                        --no-daemon \\
                                        --build-cache \\
                                        --parallel \\
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
                    slackSend(
                        channel: "#${env.slackChannel}",
                        color: 'good',
                        message: "*[${env.appEnv}] \"${env.repoName}\" 빌드 성공* :white_check_mark: \n| 브랜치: ${env.BRANCH_NAME} \n| 빌드: #${env.BUILD_NUMBER} \n| 이미지: `${env.deployVersion}` \n| <${env.BUILD_URL}/console|로그>"
                    )
                }
                failure {
                    slackSend(
                        channel: "#${env.slackChannel}",
                        color: 'danger',
                        message: "*[${env.appEnv}] \"${env.repoName}\" 빌드 실패* :x: \n| 브랜치: ${env.BRANCH_NAME} \n| 빌드: #${env.BUILD_NUMBER} \n| 이미지: `${env.deployVersion}` \n| <${env.BUILD_URL}/console|로그>"
                    )
                }
                aborted {
                    slackSend(
                        channel: "#${env.slackChannel}",
                        color: 'warning',
                        message: "*[${env.appEnv}] \"${env.repoName}\" 빌드 취소* :ballot_box_with_check: \n| 브랜치: ${env.BRANCH_NAME} \n| 빌드: #${env.BUILD_NUMBER} \n| 이미지: `${env.deployVersion}` \n| <${env.BUILD_URL}/console|로그>"
                    )
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
    }
}

