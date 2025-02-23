import grpc from '@grpc/grpc-js';
import protoLoader from '@grpc/proto-loader';
import { promisify } from 'util';

const SUMMARIZER_URL = process.env.GRPC_LISTEN_ADDRESS ?? 'localhost:50051';
const PROTO_PATH = '../proto/summarizer.proto';

// Load gRPC definitions
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const proto: any = grpc.loadPackageDefinition(packageDefinition);
export type SummarizePrompt = {
    prompt: string;
    transcript: string;
    model: string;
};

/**
 * Calls the summarize RPC and streams the response.
 * @param prompt The prompt
 */
export async function summarize(prompt: SummarizePrompt): Promise<string> {
    const client = new proto.Summarizer(SUMMARIZER_URL, grpc.credentials.createInsecure());
    const summarizePromise = promisify(client.summarize).bind(client);
    try {
        const response = await summarizePromise(prompt);
        return response.text;
    } catch (error) {
        return Promise.reject(error);
    }
}

/**
 * Calls the models method to check which models are available
 * @returns A list of models
 */
export async function models(): Promise<Array<string>> {
    const client = new proto.Summarizer(SUMMARIZER_URL, grpc.credentials.createInsecure());
    const modelsPromise = promisify(client.models).bind(client);
    try {
        const response = await modelsPromise({});
        return response.models;
    } catch (error) {
        return Promise.reject(error);
    }
}

/**
 * Calls the heartbeat method to check if the server is responsive.
 * @returns A boolean indicating server status
 */
export async function heartbeat(): Promise<boolean> {
    const client = new proto.Summarizer(SUMMARIZER_URL, grpc.credentials.createInsecure());
    const heartbeatPromise = promisify(client.heartbeat).bind(client);
    try {
        await heartbeatPromise({});
        return true;
    } catch (error) {
        return false;
    }
}
