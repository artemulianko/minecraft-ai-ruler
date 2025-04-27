import OpenAI from "openai";
import * as Shared from "openai/resources/shared";

export class AIProcessor {
    constructor(
        private readonly aiClient: OpenAI,
        private readonly model: Shared.ResponsesModel = 'gpt-4o-mini',
        private readonly role: string,
    ) {
    }

    public async process(userInput: string): Promise<string> {
        const result = await this.aiClient.responses.create({
            model: this.model,
            instructions: this.role,
            input: userInput
        });

        return result.output_text;
    }
}