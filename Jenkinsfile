pipeline {
    agent { docker { image 'maven' } }
    stages {
        stage('build') {
            steps {
                sh 'mvn compile'
            }
        }
        stage('test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('package') {
            steps {
                sh 'mvn package'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '*/target/*.jar', fingerprint: true
            junit '*/target/surefire-reports/*.xml'
        }
    }
}
