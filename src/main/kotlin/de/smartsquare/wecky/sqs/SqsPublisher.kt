package de.smartsquare.wecky.sqs

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.AmazonSQSException
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.SendMessageRequest


const val QUEUE_NAME: String = "WeckyQueue"

class SqsPublisher {

    fun publishMessage(message: String) {
        val sqs = AmazonSQSClientBuilder.defaultClient()

        val queueUrlResult = try {
            sqs.getQueueUrl(QUEUE_NAME).queueUrl
        } catch (ex: AmazonSQSException) {
            val queueRequest = CreateQueueRequest(QUEUE_NAME)
                    .addAttributesEntry("DelaySeconds", "60")
                    .addAttributesEntry("MessageRetentionPeriod", "86400")
            sqs.createQueue(queueRequest).queueUrl
        }

        val messageRequest = SendMessageRequest()
                .withQueueUrl(queueUrlResult)
                .withMessageBody(message)
                .withDelaySeconds(5)
        sqs.sendMessage(messageRequest)
    }

}