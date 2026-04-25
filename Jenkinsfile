pipeline {
    agent any

    stages {
        stage('CI Evaluation Service') {
            steps {
                build job: 'ci-evaluation-service', wait: true
            }
        }

        stage('CI Recompense Service') {
            steps {
                build job: 'ci-recompense-service', wait: true
            }
        }

        stage('CI Frontend') {
            steps {
                build job: 'ci-frontend', wait: true
            }
        }

        stage('CD Global') {
            steps {
                echo 'Tous les pipelines CI sont valides. Ajouter ici le deploiement global Docker/Kubernetes.'
            }
        }
    }
}
