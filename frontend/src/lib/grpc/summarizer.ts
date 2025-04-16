import {
    SummarizerClient,
    Prompt,
    Summary,
} from '$lib/grpc/gen/summarizer'; // Adjust path to your output folder
import { Empty } from '$lib/grpc/gen/google/protobuf/empty';

import { credentials, type ClientReadableStream } from '@grpc/grpc-js';

const SUMMARIZER_URL = process.env.GRPC_LISTEN_ADDRESS ?? 'localhost:50051';
const client = new SummarizerClient(SUMMARIZER_URL, credentials.createInsecure());

export async function summarize(
    prompt: Prompt,
    onData: (text: string) => void
): Promise<() => void> {
    return new Promise((resolve, reject) => {
        const stream: ClientReadableStream<Summary> = client.summarize(prompt);

        const onDataWrapper = (response: Summary) => {
            onData(response.text);
        };

        stream.on('data', onDataWrapper);

        stream.on('end', () => {
            resolve(() => {
                stream.removeListener('data', onDataWrapper);
            });
        });

        stream.on('error', (err) => {
            reject(err);
        });
    });
}


/**
 * Calls the models method to check which models are available
 * @returns A list of models
 */
export async function models(): Promise<string[]> {
    return new Promise((resolve, reject) => {
        client.models(Empty.create(), (err, res) => {
            if (err) return reject(err);
            resolve(res.models);
        });
    });
}

/**
 * Calls the heartbeat method to check if the server is responsive.
 * @returns A boolean indicating server status
 */
export async function heartbeat(): Promise<boolean> {
    return new Promise((resolve) => {
        client.heartbeat(Empty.create(), (err) => {
            resolve(!err);
        });
    });
}

export { type Prompt };
