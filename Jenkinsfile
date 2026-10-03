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
        // ⚠️ 关键：把 build parameter 显式映射成环境变量，之后才能用 ${XXX} 裸写引用。
        // Jenkins 的 parameters 只存在于 params.XXX，不在 Groovy binding 里；
        // 若在 sh """..."""（双引号）里直接写 ${MAVEN_PROFILE}，Groovy 会去 binding 找它，
        // 然后报：No such property: MAVEN_PROFILE for class: groovy.lang.Binding
        MAVEN_PROFILE    = "${params.MAVEN_PROFILE}"
        SKIP_TESTS       = "${params.SKIP_TESTS}"
        USE_ALIYUN_MIRROR = "${params.USE_ALIYUN_MIRROR}"
        DO_DEPLOY        = "${params.DO_DEPLOY}"

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
                    // ⚠️ 不要直接把 settings.xml 挂到 /root/.m2/settings.xml：
                    // 若宿主机上该文件不存在，Docker 会自动把它创建成"目录"，
                    // Maven 就会报 Non-readable settings ... (Is a directory)。
                    // 这里改为：整个 maven 配置目录挂到独立路径 /root/.maven-conf，
                    // 再用 mvn -s 显式指定，避免与该卷产生嵌套挂载。
                    def settingsMount = ''
                    def settingsArg   = ''
                    if (params.USE_ALIYUN_MIRROR) {
                        if (fileExists("${CICD_HOME}/maven/settings.xml")) {
                            settingsMount = "-v ${CICD_HOME}/maven:/root/.maven-conf:ro"
                            settingsArg   = '-s /root/.maven-conf/settings.xml'
                        } else {
                            echo "⚠️ 未找到 ${CICD_HOME}/maven/settings.xml，本次使用 Maven 默认中央仓库（可能很慢）"
                        }
                    }
                    // 用 Maven+JDK21 容器编译，宿主机无需安装 JDK/Maven
                    // jenkins-m2 卷做依赖缓存，第二次构建快很多
                    // 下面 ${MAVEN_PROFILE} / ${SKIP_TESTS} 来自 environment 映射，可直接插值
                    sh """
                        docker run --rm \\
                          -v "\${WORKSPACE}:/app" \\
                          -v jenkins-m2:/root/.m2 \\
                          ${settingsMount} \\
                          -w /app \\
                          ${MAVEN_IMAGE} \\
                          mvn -B -ntp ${settingsArg} \\
                            -Dmaven.repo.local=/root/.m2/repository \\
                            clean package \\
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
                    mkdir -p ${BUILD_CTX}
                    cp ${CICD_HOME}/Dockerfile                         ${BUILD_CTX}/Dockerfile
                    cp ${WORKSPACE}/ruoyi-admin/target/ruoyi-admin.jar ${BUILD_CTX}/ruoyi-admin.jar

                    docker build \
                      --label "org.opencontainers.image.revision=${GIT_COMMIT_SHORT}" \
                      --label "org.opencontainers.image.version=${IMAGE_TAG}" \
                      --build-arg APP_PORT=${CONTAINER_PORT} \
                      -t ${IMAGE_NAME}:${IMAGE_TAG} \
                      -t ${IMAGE_NAME}:latest \
                      ${BUILD_CTX}
                '''
                sh 'docker images ${IMAGE_NAME} --format "{{.Repository}}:{{.Tag}}  {{.Size}}" | head -5'
            }
        }

        stage('4. 蓝绿部署 + 健康检查 + 切换 Nginx') {
            when { expression { params.DO_DEPLOY } }
            steps {
                // ⚠️ 不要写 sudo VAR=... cmd：sudo 默认禁止传环境变量，会报
                // "sorry, you are not allowed to set the following environment variables"
                // 改成命令行参数传值，sudo 对参数不做限制。
                // ${MAVEN_PROFILE} / ${IMAGE_TAG} 等来自 environment 映射，可安全插值。
                sh """
                    sudo ${CICD_HOME}/scripts/deploy.sh \\
                      --app-name=${APP_NAME} \\
                      --image=${IMAGE_NAME}:${IMAGE_TAG} \\
                      --profile=${MAVEN_PROFILE} \\
                      --blue-port=${BLUE_PORT} \\
                      --green-port=${GREEN_PORT} \\
                      --container-port=${CONTAINER_PORT} \\
                      --health-path=${HEALTH_PATH} \\
                      --health-fallback=${HEALTH_FALLBACK} \\
                      --health-timeout=${HEALTH_TIMEOUT} \\
                      --deploy-home=${DEPLOY_HOME}
                """
            }
        }

        stage('5. 发布后验证') {
            when { expression { params.DO_DEPLOY } }
            steps {
                sh '''
                    ACTIVE_PORT=$(cat ${DEPLOY_HOME}/CURRENT_PORT)
                    echo "========== 当前活跃容器 =========="
                    docker ps --filter "name=^ruoyi-" --format "table {{.Names}}\\t{{.Image}}\\t{{.Status}}"
                    echo "========== Nginx 转发目标 =========="
                    cat /etc/nginx/conf.d/ruoyi-upstream.conf
                    echo "========== 经 Nginx 探活 =========="
                    curl -s -m 10 "http://127.0.0.1:${ACTIVE_PORT}${HEALTH_PATH}" | head -c 300; echo
                '''
            }
        }
    }

    post {
        success { echo "部署成功：${IMAGE_NAME}:${env.IMAGE_TAG}" }
        failure {
            // 注意：post 里的辅助命令一律加 || true，
            // 否则排查命令自己失败会掩盖真正的构建错误（AbortException 覆盖原始异常）
            echo '构建/部署失败，输出最近容器日志辅助排查：'
            sh 'docker ps -a --filter "name=^ruoyi-" --format "{{.Names}} {{.Status}}" || true'
            sh 'docker logs --tail 200 $(docker ps -a --filter "name=^ruoyi-" --format "{{.Names}}" | head -1) 2>/dev/null || true'
            echo '提示：真正的失败原因在本日志更靠前的位置，请往上翻找第一个 [ERROR] / 红色段落'
        }
    }
}
