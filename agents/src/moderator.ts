import {AIProcessor} from "./service/AIProcessor";
import OpenAI from "openai";
import bootstrap from "./bootstrap";
import {SendMessage} from "./generated/actions/SendMessage";
import {MutePlayer} from "./generated/actions/MutePlayer";

const aiMessageProcessor = new AIProcessor(
    new OpenAI({apiKey: process.env['OPENAI_API_KEY'] ?? ''}),
    'gpt-4o-mini',
    `
        You are chat moderator on minecraft server.
        You can perform actions in chat.
        If no need to do any actions - just return response with empty actions array.

        Available actions (TypeScript):
            type SendMessage = {
              actionType: 'SendMessage';
              content: string;
            }

            type MutePlayer =  {
              actionType: 'MutePlayer';
              playerId: string;
              mute: boolean;
              reason?: string;
              timeout?: number; // in ms
            }


        Request format (TypeScript):
            type MessagePosted = {
              content: string;
              playerId: string;
              timestamp?: number;
            }

        Response schema (TypeScript):
            type Response = {
                actions: (MutePlayer|SendMessage)[]
            }
        
        !!!IMPORTANT!!!
        Your response must be raw JSON without formatting.
    `
);

type SupportedActions = SendMessage | MutePlayer;
type SupportedActionsTypes = SendMessage['actionType'] & MutePlayer['actionType'];
type AiResponse = { actions: SupportedActions[] };

const sourceTopic = 'events.chatMessagePosted';
const topicsMap: Record<SupportedActionsTypes, string> = {
    SendMessage: 'actions.sendMessage',
    MutePlayer: 'actions.mutePlayer',
}

bootstrap(
    'moderator-client',
    'moderator-group',
    ['kafka:9092'],
    async (consumer, producer) => {
        await consumer.subscribe({topic: sourceTopic, fromBeginning: true})

        console.info('Moderator app started.');

        await consumer.run({
            eachMessage: async ({message}) => {
                try {
                    const userMessage = message.value?.toString() ?? '';

                    const response = await aiMessageProcessor.process(userMessage);
                    const responseActions: AiResponse = JSON.parse(response);

                    const executableActions = responseActions?.actions?.map((action: SendMessage | MutePlayer) => {
                        const topic = topicsMap[action.actionType as SupportedActionsTypes];
                        if (!topic) {
                            console.error('No topic to send action: ', JSON.stringify(action));
                            return null;
                        }

                        return producer.send({
                            topic: topic,
                            messages: [{value: JSON.stringify(action)}]
                        })
                    })?.filter(it => it !== null) ?? [];

                    await Promise.all(executableActions);
                } catch (e) {
                    console.error('Failed to process message: ', JSON.stringify(message));
                }
            }
        })
    }
).finally(() => console.info('Moderator app stopped.'))
