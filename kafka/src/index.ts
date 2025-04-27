import {Kafka} from "kafkajs";

(async () => {
    const client = new Kafka({
        clientId: 'initializer',
        brokers: ['localhost:9092'],
        connectionTimeout: 10000,
        requestTimeout: 30000,
    });

    const admin = client.admin();
    await admin.connect();

    // Define all topics based on the AsyncAPI specification
    const actionTopics = [
        'actions.kickPlayer',
        'actions.mutePlayer',
        'actions.sendMessage',
        'actions.spawnBlock',
        'actions.spawnCreature',
        'actions.spawnItem'
    ];

    const eventTopics = [
        'events.chatMessagePosted',
        'events.playerDamaged',
        'events.playerDied',
        'events.playerKilledEntity'
    ];

    // Create topics array for Kafka
    const topics = [...actionTopics, ...eventTopics].map(topic => ({
        topic,
        numPartitions: 3,
    }));

    try {
        await admin.createTopics({
            topics,
            validateOnly: false,
            timeout: 30000,
        });
        console.log('Created the following topics:');
        topics.forEach(topic => console.log(`- ${topic.topic}`));
    } catch (e) {
        const topicError = e as Error;

        if (topicError.message && topicError.message.includes('creation errors')) {
            console.log('Some topics already exist (this is OK)');
        } else {
            // Re-throw if it's a different error
            throw topicError;
        }
    }

    await admin.disconnect();
})()
