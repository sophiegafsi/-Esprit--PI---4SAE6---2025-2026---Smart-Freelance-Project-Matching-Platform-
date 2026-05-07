pipeline {
  agent any

  options {
    timestamps()
  }

  parameters {
    booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Run SonarQube analysis.')
    booleanParam(name: 'RUN_DOCKER_LINT', defaultValue: true, description: 'Run Hadolint on the Dockerfile.')
    booleanParam(name: 'RUN_TRIVY', defaultValue: true, description: 'Run Trivy on the built container image.')
    string(name: 'SONAR_HOST_URL', defaultValue: 'http://host.docker.internal:9000', description: 'SonarQube server URL.')
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

    stage('Hadolint') {
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
              set +e
              if [ ! -x .tools/bin/hadolint ]; then
                if command -v curl >/dev/null 2>&1; then
                  curl -fsSL -o .tools/bin/hadolint https://github.com/hadolint/hadolint/releases/download/v2.14.0/hadolint-linux-x86_64
                  download_code=\$?
                elif command -v wget >/dev/null 2>&1; then
                  wget -qO .tools/bin/hadolint https://github.com/hadolint/hadolint/releases/download/v2.14.0/hadolint-linux-x86_64
                  download_code=\$?
                else
                  echo "Neither curl nor wget is available to download hadolint." > reports/hadolint-report.txt
                  echo 127 > reports/hadolint.exitcode
                  exit 0
                fi
                if [ \$download_code -ne 0 ]; then
                  echo "Hadolint download failed." > reports/hadolint-report.txt
                  echo \$download_code > reports/hadolint.exitcode
                  exit 0
                fi
                chmod +x .tools/bin/hadolint
                chmod_code=\$?
                if [ \$chmod_code -ne 0 ]; then
                  echo "Failed to prepare hadolint binary." > reports/hadolint-report.txt
                  echo \$chmod_code > reports/hadolint.exitcode
                  exit 0
                fi
              fi
              ./.tools/bin/hadolint Dockerfile --format tty --no-fail > reports/hadolint-report.txt 2>&1
              if [ ! -s reports/hadolint-report.txt ]; then
                echo "No issues found" > reports/hadolint-report.txt
              fi
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

    stage('Trivy Image Scan') {
      when {
        expression { return params.RUN_TRIVY }
      }
      steps {
        script {
          if (isUnix()) {
            sh """
              mkdir -p reports .tools/bin .tools/trivy-cache target
              trivy_version=0.70.0
              trivy_archive=trivy_\${trivy_version}_Linux-64bit.tar.gz
              trivy_url=https://github.com/aquasecurity/trivy/releases/download/v\${trivy_version}/\${trivy_archive}
              set +e
              if [ ! -x .tools/bin/trivy ]; then
                tmpdir=\$(mktemp -d)
                if command -v curl >/dev/null 2>&1; then
                  curl -fsSL -o "\$tmpdir/\$trivy_archive" "\$trivy_url"
                  download_code=\$?
                elif command -v wget >/dev/null 2>&1; then
                  wget -qO "\$tmpdir/\$trivy_archive" "\$trivy_url"
                  download_code=\$?
                else
                  echo "Neither curl nor wget is available to download Trivy." > reports/trivy-report.txt
                  echo 127 > reports/trivy.exitcode
                  exit 0
                fi
                if [ \$download_code -ne 0 ]; then
                  echo "Trivy download failed from \$trivy_url." > reports/trivy-report.txt
                  echo \$download_code > reports/trivy.exitcode
                  rm -rf "\$tmpdir"
                  exit 0
                fi
                tar -xzf "\$tmpdir/\$trivy_archive" -C "\$tmpdir"
                extract_code=\$?
                if [ \$extract_code -ne 0 ] || [ ! -f "\$tmpdir/trivy" ]; then
                  echo "Trivy archive extraction failed." > reports/trivy-report.txt
                  echo \$extract_code > reports/trivy.exitcode
                  rm -rf "\$tmpdir"
                  exit 0
                fi
                mv "\$tmpdir/trivy" .tools/bin/trivy
                chmod +x .tools/bin/trivy
                chmod_code=\$?
                rm -rf "\$tmpdir"
                if [ \$chmod_code -ne 0 ]; then
                  echo "Failed to prepare Trivy binary." > reports/trivy-report.txt
                  echo \$chmod_code > reports/trivy.exitcode
                  exit 0
                fi
              fi
              export TRIVY_CACHE_DIR="\$PWD/.tools/trivy-cache"
              trivy_image_name=portfolio-service:latest
              ./mvnw -B com.google.cloud.tools:jib-maven-plugin:3.4.6:buildTar -DskipTests -Dimage=\$trivy_image_name -Djib.outputPaths.tar=target/jib-image.tar > reports/jib-build.log 2>&1
              jib_code=\$?
              if [ \$jib_code -ne 0 ]; then
                cat reports/jib-build.log > reports/trivy-report.txt
                echo \$jib_code > reports/trivy.exitcode
                exit 0
              fi
              set +e
              ./.tools/bin/trivy image --input target/jib-image.tar --scanners vuln --severity ${params.TRIVY_SEVERITY} --no-progress --format table -o reports/trivy-report.txt
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
      script {
        try {
          emailext(
            subject: "Build ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
            body: """Build result: ${currentBuild.currentResult}

Job: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
URL: ${env.BUILD_URL}
""",
            to: 'fares.belgacem@esprit.tn'
          )
        } catch (err) {
          echo "Email notification failed: ${err.getMessage()}"
        }
      }
    }
  }
}
