pipeline {
    agent any

    parameters {
        choice(
            name: 'OS',
            choices: ['linux', 'darwin', 'windows'],
            description: 'Target operating system'
        )
        choice(
            name: 'ARCH',
            choices: ['amd64', 'arm64'],
            description: 'Target architecture'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip running tests'
        )
        booleanParam(
            name: 'SKIP_LINT',
            defaultValue: true,
            description: 'Skip running linter'
        )
    }

    environment {
        REPO = '/home/oleh/backup/Jenkins/kbot'
        BRANCH = 'main'
        IMAGE_NAME = 'oskrypnykov/kbot'
        PATH = "/home/oleh/go/bin:/usr/local/go/bin:${env.PATH}"
    }

    stages {
        stage('clone') {
            steps {
                echo "Cloning the repository..."
                git branch: "${BRANCH}", url: "${REPO}"
            }
        }

        stage('lint') {
            when {
                expression { return !params.SKIP_LINT }
            }
            steps {
                echo "Running linter..."
                sh 'make lint'
            }
        }

        stage('test') {
            when {
                expression { return !params.SKIP_TESTS }
            }
            steps {
                echo "Running tests..."
                sh 'make test'
            }
        }

        stage('build') {
            steps {
                echo "Building the application for ${params.OS}/${params.ARCH}..."
                sh "make build TARGETOS=${params.OS} TARGETARCH=${params.ARCH}"
            }
        }

        stage('image') {
            steps {
                script {
                    echo "Building Docker image: ${IMAGE_NAME} for ${params.OS}/${params.ARCH}"
                    sh "make image TARGETOS=${params.OS} TARGETARCH=${params.ARCH}"
                }
            }
        }

        stage('push') {
            steps {
                script {
                    echo "Pushing the image..."
                    docker.withRegistry('', 'dockerhub') {
                        sh "make push TARGETOS=${params.OS} TARGETARCH=${params.ARCH}"
                    }
                }
            }
        }
    }
}
