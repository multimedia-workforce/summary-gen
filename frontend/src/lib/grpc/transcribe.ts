import grpc from '@grpc/grpc-js';
import protoLoader from '@grpc/proto-loader';
import { promisify } from 'util';

const TRANSCRIBER_URL = process.env.GRPC_LISTEN_ADDRESS ?? 'localhost:50051';
const PROTO_PATH = '../proto/transcriber.proto';

// Load gRPC definitions
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const proto: any = grpc.loadPackageDefinition(packageDefinition);

type TranscribeCallback = (message: string) => void;

/**
 * Sends an file buffer to the gRPC transcribe service and receives a transcript stream.
 * @param reader The video/audio file as a readable stream
 * @returns Promise
 */
export async function transcribe(reader: ReadableStreamDefaultReader, callback: TranscribeCallback): Promise<void> {
    const client = new proto.Transcriber(TRANSCRIBER_URL, grpc.credentials.createInsecure());
    return new Promise(async (resolve, reject) => {
        const call = client.transcribe();

        call.on('error', (error: Error) => {
            reject(error.message);
        });

        call.on('end', () => {
            resolve();
        });

        try {
            const chunkSize = 64 * 1024;
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                let buffer = Buffer.from(value);
                while (buffer.length > 0) {
                    const chunk = buffer.subarray(0, Math.min(chunkSize, buffer.length));
                    buffer = buffer.subarray(chunk.length);
                    call.write({ data: chunk });
                }
            }

            call.end();
        } catch (error) {
            reject(error);
        }

        call.on('data', (transcription: any) => {
            callback(transcription.text);
        });
    });
}



/**
 * Calls the heartbeat method to check if the server is responsive.
 * @returns A boolean indicating server status
 */
export async function heartbeat(): Promise<boolean> {
    const client = new proto.Transcriber(TRANSCRIBER_URL, grpc.credentials.createInsecure());
    const heartbeatPromise = promisify(client.heartbeat).bind(client);
    try {
        await heartbeatPromise({});
        return true;
    } catch (error) {
        return false;
    }
}
