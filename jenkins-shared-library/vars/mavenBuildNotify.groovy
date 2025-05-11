def call(Map params) {
    // Repository URL and Branch
    def repoUrl = params.repoUrl ?: error("Repository URL is required")
    def branch = params.branch ?: 'main' // Default to 'main' branch
    def mavenCommand = params.mavenCommand ?: 'clean install' // Default Maven command
    def notificationType = params.notificationType ?: 'console' // Default notification type: console
    def slackWebhookUrl = params.slackWebhookUrl ?: ''
    def emailRecipients = params.emailRecipients ?: ''

    // Clone the repository from the provided URL and branch
    checkout scm: [
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        extensions: [],
        userRemoteConfigs: [[url: repoUrl]]
    ]

    // Run the Maven build command
    echo "Running Maven Command: mvn ${mavenCommand}"
    sh "mvn ${mavenCommand}"

    // Send Notification after the build
    sendNotification(notificationType, repoUrl, branch, mavenCommand)
}

// Send notification to Slack or Email based on the type
def sendNotification(String notificationType, String repoUrl, String branch, String mavenCommand) {
    def buildStatus = currentBuild.result ?: 'SUCCESS'
    
    def message = "Build Status: ${buildStatus}\n" +
                  "Repository URL: ${repoUrl}\n" +
                  "Branch: ${branch}\n" +
                  "Maven Command: ${mavenCommand}\n"

    // Send notification based on the type
    if (notificationType == 'slack') {
        slackNotification(message)
    } else if (notificationType == 'email') {
        emailNotification(message)
    } else {
        // Default to console log
        echo message
    }
}

// Slack notification
def slackNotification(String message) {
    if (!slackWebhookUrl) {
        error "Slack Webhook URL is missing"
    }
    slackSend(channel: '#general', message: message, webhookUrl: slackWebhookUrl)
}

// Email notification
def emailNotification(String message) {
    if (!emailRecipients) {
        error "Email recipients are missing"
    }
    emailext (
        subject: "Build Notification: ${currentBuild.fullDisplayName}",
        body: message,
        to: emailRecipients
    )
}
