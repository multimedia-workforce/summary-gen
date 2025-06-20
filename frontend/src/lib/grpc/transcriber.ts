import {
    TranscriberClient,
    Chunk,
    Transcript,
} from '$lib/grpc/gen/transcriber';
import {Empty} from '$lib/grpc/gen/google/protobuf/empty';

import {credentials, type ClientDuplexStream} from '@grpc/grpc-js';

const TRANSCRIBER_URL = process.env.GRPC_LISTEN_ADDRESS ?? 'localhost:50051';
const client = new TranscriberClient(TRANSCRIBER_URL, credentials.createInsecure());

export type TranscribeCallback = (id: string, message: string) => void;

/**
 * Sends a stream to the gRPC transcribe service and receives transcript stream.
 * @param userId The ID of the user
 * @param reader The video/audio readable stream reader
 * @param callback Called with transcript text as it's received
 */
export async function transcribe(
    userId: string,
    reader: ReadableStreamDefaultReader,
    callback: TranscribeCallback
): Promise<() => void> {
    return new Promise(async (resolve, reject) => {
        const call: ClientDuplexStream<Chunk, Transcript> = client.transcribe();

        const onData = (response: Transcript) => {
            callback(response.id, response.text);
        };

        call.on('data', onData);
        call.on('end', () => resolve(() => call.removeListener('data', onData)));
        call.on('error', (err) => reject(err));

        const chunkSize = 64 * 1024;

        try {
            while (true) {
                const {done, value} = await reader.read();
                if (done) break;

                let buffer = Buffer.from(value);
                while (buffer.length > 0) {
                    const chunk = buffer.subarray(0, Math.min(chunkSize, buffer.length));
                    buffer = buffer.subarray(chunk.length);
                    call.write(Chunk.fromPartial({userId: userId, data: chunk}));
                }
            }

            call.end();
        } catch (error) {
            reject(error);
        }
    });
}

export async function heartbeat(): Promise<boolean> {
    return new Promise((resolve) => {
        client.heartbeat(Empty.create(), (err) => {
            resolve(!err);
        });
    });
}
