import org.contralib.Utils

/**
 * Example Usage:
 * msgProperties = ['msgTopic': 'stageName', 'msgProps': 'stage=stage1', 'msgContent': 'pipelineResult=success']
 * sendMessageWithAudit(msgProperties)
 *
 * @param parameters
 * @return
 */

def call(Map parameters = [:]) {
    def msgTopic = parameters.get('msgTopic')
    def msgProps = parameters.get('msgProps')
    def msgContent = parameters.get('msgContent')
    def msgAuditFile = parameters.get('msgAuditFile', 'auditfile.json')
    def msgRetryCount = parameters.get('msgRetryCount', 3)

    // Get contents of auditFile
    def auditContent = readJSON file: msgAuditFile

    // Send message and get handle on SendResult
    def sendResult = sendMessage(msgTopic, msgProps, msgContent)

    String id = sendResult.getMessageId()
    String msg = sendResult.getMessageContent()

    auditContent[id] = msg

    // write to auditFile and archive
    writeJSON pretty: 4, file: msgAuditFile, json: auditContent

    archiveArtifacts allowEmptyArchive: false, artifacts: msgAuditFile

    trackMessage(id, msgRetryCount)
}
