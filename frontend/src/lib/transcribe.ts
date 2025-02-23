import grpc from '@grpc/grpc-js';
import protoLoader from '@grpc/proto-loader';
import { promisify } from 'util';

const TRANSCRIBER_URL = process.env.MEETING_SUM_TRANSCRIBER_URL ?? 'localhost:50051';
const PROTO_DIRECTORY = '../proto';
const PROTO_PATH = `${PROTO_DIRECTORY}/transcriber.proto`;

// Load gRPC definitions
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const proto: any = grpc.loadPackageDefinition(packageDefinition);
const client = new proto.Transcriber(TRANSCRIBER_URL, grpc.credentials.createInsecure());

type TranscribeCallback = (message: string) => void;

/**
 * Sends an MP4 file buffer to the gRPC transcribe service and receives a transcript stream.
 * @param videoBuffer - The video file as a Buffer
 * @returns Async Generator yielding transcribed text
 */
export async function transcribe(videoBuffer: Buffer, callback: TranscribeCallback) {
    const call = client.transcribe();
    
    // Create a readable stream from the buffer and send it in chunks
    const chunkSize = 64 * 1024; // 64 KB per chunk
    for (let i = 0; i < videoBuffer.length; i += chunkSize) {
        const chunk = videoBuffer.subarray(i, i + chunkSize);
        try {
            call.write({ data: chunk });
        } catch(error) {
            return Promise.reject((error as Error).message);
        }
    }
    
    call.end(); // Signal end of transmission
    call.on('data', (transcription: any) => {
        callback(transcription.text);
    });
}

/**
 * Calls the heartbeat method to check if the server is responsive.
 * @returns A boolean indicating server status
 */
export async function heartbeat(): Promise<boolean> {
    const heartbeatPromise = promisify(client.heartbeat).bind(client);
    try {
        await heartbeatPromise({});
        return true;
    } catch (error) {
        return false;
    }
}
