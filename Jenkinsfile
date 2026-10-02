// RuoYi-Vue-Plus 6.X（CoDeleven/bohilltravel_6.X_dev）构建 + 镜像 + 蓝绿部署流水线
// 放置位置：/opt/ruoyi-cicd/Jenkinsfile
// 关键事实（来自仓库实检）：
//   - 6.X 基线为 JDK 21 + Spring Boot 4，编译与运行都必须用 21
//   - ruoyi-admin/src/main/resources/application.yml 中 server.port = 8088
//   - spring.profiles.active 使用 @profiles.active@ 占位符 → 必须用 -P 指定 profile 触发资源过滤
//   - management.endpoints.web.exposure.include: '*' → /actuator/health 可直接用于健康检查
//   - 打包产物 finalName 为 ruoyi-admin → ruoyi-admin/target/ruoyi-admin.jar

pipeline {
    agent any

    parameters {
        string(name: 'GIT_BRANCH', defaultValue: 'bohilltravel_6.X_dev',
               description: '要构建的分支')
        string(name: 'GIT_REPO_URL', defaultValue: 'https://github.com/CoDeleven/RuoYi-Vue-Plus.git',
               description: '仓库地址；国内服务器拉不动 GitHub 时可换成 Gitee/镜像地址')
        // 公开仓库留空即可匿名拉取；填了就用 Jenkins 里对应 ID 的凭据
        string(name: 'GIT_CREDENTIALS_ID', defaultValue: '',
               description: 'Jenkins 凭据ID（Username with password / PAT）。私有仓库必填，公开仓库可留空')
        choice(name: 'MAVEN_PROFILE', choices: ['prod', 'dev', 'local'],
               description: 'Maven profile，对应 application-{profile}.yml（dev 为 pom 默认）')
        booleanParam(name: 'SKIP_TESTS', defaultValue: true, description: '跳过单元测试')
        booleanParam(name: 'USE_ALIYUN_MIRROR', defaultValue: true, description: '使用阿里云 Maven 镜像加速')
        booleanParam(name: 'DO_DEPLOY', defaultValue: true, description: '是否执行部署（关闭则只构建镜像）')
    }

    environment {
        APP_NAME       = 'ruoyi-vue-plus'
        IMAGE_NAME     = 'ruoyi/ruoyi-server'      // 构建出的镜像名
        CICD_HOME      = '/opt/ruoyi-cicd'         // 本套脚本在宿主机的存放目录
        BUILD_CTX      = '/opt/ruoyi-cicd/build'   // docker build 上下文（jar + Dockerfile）
        DEPLOY_HOME    = '/opt/ruoyi'              // 运行目录：config / logs / 状态文件
        BLUE_PORT      = '9091'                    // 蓝环境宿主机端口
        GREEN_PORT     = '9092'                    // 绿环境宿主机端口
        // 仓库 application.yml 中 server.port = 8088，这里必须与之一致
        CONTAINER_PORT = '8088'
        HEALTH_PATH    = '/actuator/health'        // 主健康检查路径（6.X 已暴露全部端点）
        HEALTH_FALLBACK = '/auth/code'             // 备选路径
        HEALTH_TIMEOUT = '240'                     // 健康检查最长等待（秒），Jetty 冷启动较慢
        // 6.X 基线：JDK 21 + Spring Boot 4
        MAVEN_IMAGE    = 'maven:3.9-eclipse-temurin-21'
    }

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
        disableConcurrentBuilds()   // 防止并发部署互相抢端口
    }

    stages {

        stage('1. 拉取源码') {
            steps {
                deleteDir()
                // credentialsId 为空字符串 = 匿名拉取，公开仓库够用
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${params.GIT_BRANCH}"]],
                    userRemoteConfigs: [[
                        url: "${params.GIT_REPO_URL}",
                        credentialsId: "${params.GIT_CREDENTIALS_ID?.trim() ?: ''}"
                    ]]
                ])
                script {
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                }
                echo "分支: ${params.GIT_BRANCH}  提交: ${env.GIT_COMMIT_SHORT}  镜像: ${IMAGE_NAME}:${env.IMAGE_TAG}"
            }
        }

        stage('2. Maven 编译打包（JDK 21）') {
            steps {
                script {
                    def mirrorOpt = params.USE_ALIYUN_MIRROR \
                        ? '-v ${CICD_HOME}/maven/settings.xml:/root/.m2/settings.xml:ro' : ''
                    // 用 Maven+JDK21 容器编译，宿主机无需安装 JDK/Maven
                    // jenkins-m2 卷做依赖缓存，第二次构建快很多
                    sh """
                        docker run --rm \\
                          -v "\${WORKSPACE}:/app" \\
                          -v jenkins-m2:/root/.m2 \\
                          ${mirrorOpt} \\
                          -w /app \\
                          ${MAVEN_IMAGE} \\
                          mvn -B -ntp clean package \\
                            -P ${MAVEN_PROFILE} \\
                            -Dmaven.test.skip=${SKIP_TESTS}
                    """
                }
                // 容器内以 root 生成产物，改回 jenkins 属主，避免下次清理 workspace 失败
                sh 'docker run --rm -v "${WORKSPACE}:/app" alpine chown -R $(id -u):$(id -g) /app || true'
                sh 'ls -lh ruoyi-admin/target/ruoyi-admin.jar'
            }
        }

        stage('3. 构建 Docker 镜像') {
            steps {
                sh '''
                    sudo mkdir -p ${BUILD_CTX}
                    sudo cp ${CICD_HOME}/Dockerfile                         ${BUILD_CTX}/Dockerfile
                    sudo cp ${WORKSPACE}/ruoyi-admin/target/ruoyi-admin.jar ${BUILD_CTX}/ruoyi-admin.jar

                    sudo docker build \
                      --label "org.opencontainers.image.revision=${GIT_COMMIT_SHORT}" \
                      --label "org.opencontainers.image.version=${IMAGE_TAG}" \
                      --build-arg APP_PORT=${CONTAINER_PORT} \
                      -t ${IMAGE_NAME}:${IMAGE_TAG} \
                      -t ${IMAGE_NAME}:latest \
                      ${BUILD_CTX}
                '''
                sh 'sudo docker images ${IMAGE_NAME} --format "{{.Repository}}:{{.Tag}}  {{.Size}}" | head -5'
            }
        }

        stage('4. 蓝绿部署 + 健康检查 + 切换 Nginx') {
            when { expression { params.DO_DEPLOY } }
            steps {
                sh '''
                    sudo APP_NAME="${APP_NAME}" \
                         IMAGE="${IMAGE_NAME}:${IMAGE_TAG}" \
                         SPRING_PROFILE="${MAVEN_PROFILE}" \
                         BLUE_PORT="${BLUE_PORT}" \
                         GREEN_PORT="${GREEN_PORT}" \
                         CONTAINER_PORT="${CONTAINER_PORT}" \
                         HEALTH_PATH="${HEALTH_PATH}" \
                         HEALTH_FALLBACK="${HEALTH_FALLBACK}" \
                         HEALTH_TIMEOUT="${HEALTH_TIMEOUT}" \
                         DEPLOY_HOME="${DEPLOY_HOME}" \
                         ${CICD_HOME}/scripts/deploy.sh
                '''
            }
        }

        stage('5. 发布后验证') {
            when { expression { params.DO_DEPLOY } }
            steps {
                sh '''
                    ACTIVE_PORT=$(sudo cat ${DEPLOY_HOME}/CURRENT_PORT)
                    echo "========== 当前活跃容器 =========="
                    sudo docker ps --filter "name=^ruoyi-" --format "table {{.Names}}\\t{{.Image}}\\t{{.Status}}"
                    echo "========== Nginx 转发目标 =========="
                    sudo cat /etc/nginx/conf.d/ruoyi-upstream.conf
                    echo "========== 经 Nginx 探活 =========="
                    curl -s -m 10 "http://127.0.0.1:${ACTIVE_PORT}${HEALTH_PATH}" | head -c 300; echo
                '''
            }
        }
    }

    post {
        success { echo "部署成功：${IMAGE_NAME}:${env.IMAGE_TAG}" }
        failure {
            echo '构建/部署失败，输出最近容器日志辅助排查：'
            sh 'sudo docker ps -a --filter "name=^ruoyi-" --format "{{.Names}} {{.Status}}"'
            sh 'sudo docker logs --tail 200 $(sudo docker ps -a --filter "name=^ruoyi-" --format "{{.Names}}" | head -1) 2>/dev/null || true'
        }
    }
}
