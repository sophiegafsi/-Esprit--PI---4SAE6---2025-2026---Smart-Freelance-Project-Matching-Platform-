pipeline {
  agent any

  options {
    timestamps()
  }

  parameters {
    booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Run SonarQube analysis.')
    booleanParam(name: 'RUN_DOCKER_LINT', defaultValue: true, description: 'Run Hadolint on the Dockerfile.')
    booleanParam(name: 'RUN_TRIVY', defaultValue: true, description: 'Run Trivy filesystem scan.')
    string(name: 'SONAR_HOST_URL', defaultValue: 'http://localhost:9000', description: 'SonarQube server URL.')
    string(name: 'TRIVY_SEVERITY', defaultValue: 'HIGH,CRITICAL', description: 'Trivy severities to report.')
  }

  stages {
    stage('Test') {
      steps {
        script {
          if (isUnix()) {
            sh 'chmod +x mvnw && ./mvnw -B clean test'
          } else {
            bat 'mvnw.cmd -B clean test'
          }
        }
      }
    }

    stage('Package') {
      steps {
        script {
          if (isUnix()) {
            sh './mvnw -B package -DskipTests'
          } else {
            bat 'mvnw.cmd -B package -DskipTests'
          }
        }
      }
    }

    stage('Dockerfile Lint') {
      when {
        allOf {
          expression { return params.RUN_DOCKER_LINT }
          expression { return fileExists('Dockerfile') }
        }
      }
      steps {
        script {
          if (isUnix()) {
            sh """
              mkdir -p reports .tools/bin
              if [ ! -x .tools/bin/hadolint ]; then
                if command -v curl >/dev/null 2>&1; then
                  curl -fsSL -o .tools/bin/hadolint https://github.com/hadolint/hadolint/releases/download/v2.14.0/hadolint-linux-x86_64
                elif command -v wget >/dev/null 2>&1; then
                  wget -qO .tools/bin/hadolint https://github.com/hadolint/hadolint/releases/download/v2.14.0/hadolint-linux-x86_64
                else
                  echo "Neither curl nor wget is available to download hadolint." > reports/hadolint-report.txt
                  echo 127 > reports/hadolint.exitcode
                  exit 0
                fi
                chmod +x .tools/bin/hadolint || {
                  echo "Failed to prepare hadolint binary." > reports/hadolint-report.txt
                  echo 126 > reports/hadolint.exitcode
                  exit 0
                }
              fi
              set +e
              ./.tools/bin/hadolint Dockerfile --format tty > reports/hadolint-report.txt 2>&1
              echo \$? > reports/hadolint.exitcode
              exit 0
            """
          } else {
            bat '''
              if not exist reports mkdir reports
              echo Hadolint auto-install is only configured for Unix Jenkins agents.> reports\\hadolint-report.txt
              echo 127> reports\\hadolint.exitcode
              exit /b 0
            '''
          }
        }
      }
    }

    stage('Trivy Scan') {
      when {
        expression { return params.RUN_TRIVY }
      }
      steps {
        script {
          if (isUnix()) {
            sh """
              mkdir -p reports .tools/bin .tools/trivy-cache
              if [ ! -x .tools/bin/trivy ]; then
                tmpdir=\$(mktemp -d)
                if command -v curl >/dev/null 2>&1; then
                  curl -fsSL -o "\$tmpdir/trivy.tar.gz" https://github.com/aquasecurity/trivy/releases/download/v0.67.2/trivy_0.67.2_Linux-64bit.tar.gz
                elif command -v wget >/dev/null 2>&1; then
                  wget -qO "\$tmpdir/trivy.tar.gz" https://github.com/aquasecurity/trivy/releases/download/v0.67.2/trivy_0.67.2_Linux-64bit.tar.gz
                else
                  echo "Neither curl nor wget is available to download Trivy." > reports/trivy-report.txt
                  echo 127 > reports/trivy.exitcode
                  exit 0
                fi
                tar -xzf "\$tmpdir/trivy.tar.gz" -C "\$tmpdir" || {
                  echo "Failed to extract Trivy archive." > reports/trivy-report.txt
                  echo 126 > reports/trivy.exitcode
                  rm -rf "\$tmpdir"
                  exit 0
                }
                mv "\$tmpdir/trivy" .tools/bin/trivy
                chmod +x .tools/bin/trivy
                rm -rf "\$tmpdir"
              fi
              export TRIVY_CACHE_DIR="\$PWD/.tools/trivy-cache"
              set +e
              ./.tools/bin/trivy fs --scanners vuln,secret,misconfig --severity ${params.TRIVY_SEVERITY} --no-progress --format table -o reports/trivy-report.txt .
              echo \$? > reports/trivy.exitcode
              exit 0
            """
          } else {
            bat """
              if not exist reports mkdir reports
              echo Trivy auto-install is only configured for Unix Jenkins agents.> reports\\trivy-report.txt
              echo 127> reports\\trivy.exitcode
              exit /b 0
            """
          }
        }
      }
    }

    stage('SonarQube') {
      when {
        expression { return params.RUN_SONAR }
      }
      steps {
        withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
          script {
            if (isUnix()) {
              sh "chmod +x mvnw && ./mvnw -B sonar:sonar -Dsonar.projectKey=pidev-portfolio-service -Dsonar.host.url=${params.SONAR_HOST_URL} -Dsonar.token=\$SONAR_TOKEN"
            } else {
              bat "mvnw.cmd -B sonar:sonar -Dsonar.projectKey=pidev-portfolio-service -Dsonar.host.url=${params.SONAR_HOST_URL} -Dsonar.token=%SONAR_TOKEN%"
            }
          }
        }
      }
    }
  }

  post {
    always {
      junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
      archiveArtifacts artifacts: 'target/*-SNAPSHOT.jar', fingerprint: true, allowEmptyArchive: true
      archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
      emailext(
        subject: "Build ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
        body: """Build result: ${currentBuild.currentResult}

Job: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
URL: ${env.BUILD_URL}
""",
        to: 'fares.belgacem@esprit.tn'
      )
    }
  }
}
