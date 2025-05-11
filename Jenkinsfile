pipeline {
    agent { label "node1" }
    tools {
        maven 'mvn'
        jdk 'JDK'
    }
    environment {
        GIT_URL = 'https://github.com/vaadin/addressbook.git'
    }
    parameters {
        choice(name: 'BUILD_TYPE', choices: ['Development', 'Staging', 'Production'], description: 'Choose your environment')
    }
    stages {
        stage('Approval') {
            steps {
                script {
                    input message: 'Proceed to Build and Package?', ok: 'Yes, Continue'
                }
            }
        }
        stage('Code Checkout') {
            steps {
                git url: "${env.GIT_URL}"
            }
        }
        stage('Build and Test in Parallel') {
            parallel {
                stage('Compile') {
                    steps {
                        sh 'mvn compile'
                    }
                }
                stage('Code Review') {
                    steps {
                        sh 'mvn findbugs:findbugs'
                    }
                }
                stage('Unit Test') {
                    steps {
                        sh 'mvn test'
                    }
                }
            }
        }
        stage('Package') {
            steps {
                echo "Building for environment: ${params.BUILD_TYPE}"
                sh 'mvn package'
            }
        }
    }
}
