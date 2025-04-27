import 'dotenv/config'
import {Kafka, Consumer, Producer} from "kafkajs";

export default async (
    clientId: string,
    groupId: string,
    brokers: string[],
    handler: (consumer: Consumer, producer: Producer) => Promise<void>
) => {
    const kafka = new Kafka({
        clientId,
        brokers
    });
    const consumer = kafka.consumer({groupId});
    const producer = kafka.producer();
    await consumer.connect()
    await producer.connect();

    await handler(consumer, producer);
}