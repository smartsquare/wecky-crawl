package de.smartsquare.wecky.sqs

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.AmazonSQSException
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.SendMessageRequest


const val QUEUE_NAME: String = "WeckyQueue"

class SqsPublisher {

    fun publishMessage(msg: String) {
        val sqs = AmazonSQSClientBuilder.defaultClient()

        val queueUrlResult = try {
            sqs.getQueueUrl(QUEUE_NAME).queueUrl
        } catch (ex: AmazonSQSException) {
            val create_request = CreateQueueRequest(QUEUE_NAME)
                    .addAttributesEntry("DelaySeconds", "60")
                    .addAttributesEntry("MessageRetentionPeriod", "86400")
            val createQueueResult = sqs.createQueue(create_request)
            createQueueResult.queueUrl
        }

        val send_msg_request = SendMessageRequest()
                .withQueueUrl(queueUrlResult)
                .withMessageBody(msg)
                .withDelaySeconds(5)
        sqs.sendMessage(send_msg_request)
    }
}