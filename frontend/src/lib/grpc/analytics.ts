import {
    AnalyticsClient,
    SmartSessionPromptRequest,
    SmartSessionPromptResponse,
} from "$lib/grpc/gen/analytics"; // Adjust path to your output folder

import {credentials, type ClientReadableStream} from "@grpc/grpc-js";

const ANALYTICS_URL = process.env.ANALYTICS_GRPC_URL ?? "localhost:50053";
const client = new AnalyticsClient(ANALYTICS_URL, credentials.createInsecure());

export async function smartSessionPrompt(
    prompt: SmartSessionPromptRequest,
    onData: (text: string) => void,
): Promise<() => void> {
    return new Promise((resolve, reject) => {
        const stream: ClientReadableStream<SmartSessionPromptResponse> =
            client.handleSmartSessionPrompt(prompt);

        const onDataWrapper = (response: SmartSessionPromptResponse) => {
            onData(response.chunk);
        };

        stream.on("data", onDataWrapper);

        stream.on("end", () => {
            resolve(() => {
                stream.removeListener("data", onDataWrapper);
            });
        });

        stream.on("error", (err) => {
            reject(err);
        });
    });
}

export {type SmartSessionPromptRequest};
